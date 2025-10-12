package com.interface21.jdbc.core;

import com.interface21.dao.DataMappingException;
import com.interface21.jdbc.core.conversion.TypeConversionService;
import com.interface21.jdbc.core.util.NamingUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code ColumnMatchingRowMapper}는 ResultSet의 컬럼명과 자바 객체의 생성자 파라미터 또는 필드명을 자동 매칭하여 객체를 생성하는 RowMapper 구현체입니다.
 * <p>
 * 컬럼명이 snake_case일 경우 camelCase로 변환하여 매핑합니다.
 * <ul>
 *   <li>파라미터가 많은 생성자를 우선적으로 사용하며, 생성자 파라미터명과 컬럼명이 일치하면 생성자를 통해 객체를 생성합니다.</li>
 *   <li>생성자 매핑이 어려운 경우(기본 생성자 또는 파라미터명 불일치)에는 필드 주입 방식으로 객체를 생성합니다.</li>
 * </ul>
 * <p>
 * 필드 주입 방식은 기본 생성자가 필요합니다.
 * 생성자 기반 매핑은 파라미터 이름 정보를 유지하기 위해 컴파일 시 {@code -parameters} 옵션이 활성화되어야 합니다.
 *
 * @param <T> 매핑할 객체 타입
 */
public class ColumnMatchingRowMapper<T> implements RowMapper<T> {

    private final Class<T> mappedClass;
    private final Constructor<?> constructor;
    private final Map<String, Integer> parameterIndexMap;
    private final Map<String, Field> fieldCache;

    /**
     * 매핑할 클래스 타입을 받아 RowMapper를 생성합니다.
     *
     * @param mappedClass ResultSet에서 매핑할 대상 클래스
     */
    public ColumnMatchingRowMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.constructor = findWidestConstructor();
        this.parameterIndexMap = buildParameterIndexMap(constructor);
        this.fieldCache = buildFieldCache();
    }

    /**
     * ResultSet의 현재 행을 객체로 매핑합니다.
     * <p>
     * 생성자 주입 또는 필드 주입 전략 중 적합한 방식을 자동 선택하여 객체를 반환합니다.
     *
     * @param rs ResultSet 객체
     * @return 매핑된 객체 인스턴스
     * @throws SQLException ResultSet 접근 오류 발생 시
     */
    @Override
    public T mapRow(ResultSet rs) throws SQLException {
        if (isFieldInjectionPreferred()) {
            return mapUsingFieldInjection(rs);
        }
        return mapUsingParameterizedConstructor(rs);
    }

    private boolean isFieldInjectionPreferred() {
        return constructor == null
               || constructor.getParameterCount() == 0
               || parameterIndexMap.containsKey("arg0");
    }

    private Constructor<?> findWidestConstructor() {
        return Arrays.stream(mappedClass.getDeclaredConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .map(c -> {
                    c.setAccessible(true);
                    return c;
                })
                .orElse(null);
    }

    private Map<String, Integer> buildParameterIndexMap(Constructor<?> constructor) {
        if (constructor == null) {
            return Map.of();
        }
        Parameter[] parameters = constructor.getParameters();
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            indexMap.put(parameters[i].getName(), i);
        }
        return indexMap;
    }

    private Map<String, Field> buildFieldCache() {
        Map<String, Field> cache = new HashMap<>();
        Field[] fields = mappedClass.getDeclaredFields();
        for (Field field : fields) {
            String camelCaseName = NamingUtils.snakeToCamel(field.getName().toLowerCase());
            field.setAccessible(true);
            cache.put(camelCaseName, field);
        }
        return cache;
    }

    /**
     * 생성자 파라미터명을 컬럼명과 매칭하여 객체를 생성합니다.
     * <p>
     * 컬럼명과 생성자 파라미터명이 일치할 때, 타입 변환을 수행하여 생성자를 호출합니다.
     *
     * @param rs ResultSet 객체
     * @return 생성자 기반으로 매핑된 객체
     */
    private T mapUsingParameterizedConstructor(ResultSet rs) {
        try {
            Object[] args = resolveConstructorArguments(rs);
            return (T) constructor.newInstance(args);
        } catch (Exception e) {
            throw new DataMappingException("생성자를 통한 객체 매핑 실패: " + mappedClass.getSimpleName(), e);
        }
    }

    private Object[] resolveConstructorArguments(ResultSet rs) throws SQLException {
        Object[] args = new Object[constructor.getParameterCount()];
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (int i = 1; i <= columnCount; i++) {
            String label = NamingUtils.snakeToCamel(rsmd.getColumnLabel(i).toLowerCase());
            assignConstructorArgument(args, rs, i, parameterTypes, label);
        }
        return args;
    }

    private void assignConstructorArgument(Object[] args, ResultSet rs, int columnIndex,
                                           Class<?>[] parameterTypes, String label) throws SQLException {
        Integer index = parameterIndexMap.get(label);
        if (index != null) {
            Object value = rs.getObject(columnIndex);
            Class<?> parameterType = parameterTypes[index];
            args[index] = TypeConversionService.convert(value, parameterType);
        }
    }

    /**
     * 기본 생성자로 객체를 생성한 뒤, 컬럼명과 필드명을 매칭하여 직접 값을 주입합니다.
     * <p>
     * 컬럼명과 필드명이 일치하면 타입 변환 후 필드에 값을 설정합니다.
     *
     * @param rs ResultSet 객체
     * @return 필드 주입 기반으로 매핑된 객체
     */
    private T mapUsingFieldInjection(ResultSet rs) {
        try {
            T mappedObject = mappedClass.getDeclaredConstructor().newInstance();
            populateFieldsFromResultSet(mappedObject, rs);
            return mappedObject;
        } catch (Exception e) {
            throw new DataMappingException("기본 생성자 기반 객체 매핑 실패: " + mappedClass.getSimpleName(), e);
        }
    }

    private void populateFieldsFromResultSet(T target, ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String label = NamingUtils.snakeToCamel(rsmd.getColumnLabel(i).toLowerCase());
            setFieldIfExists(target, label, rs.getObject(i));
        }
    }

    private void setFieldIfExists(T target, String fieldName, Object value) {
        try {
            Field field = fieldCache.get(fieldName);
            if (field == null) {
                return;
            }
            final Object convertedValue = TypeConversionService.convert(value, field.getType());
            field.set(target, convertedValue);
        } catch (Exception e) {
            throw new DataMappingException("필드 설정 실패: " + fieldName, e);
        }
    }
}
