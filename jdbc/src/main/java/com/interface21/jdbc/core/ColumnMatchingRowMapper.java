package com.interface21.jdbc.core;

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

public class ColumnMatchingRowMapper<T> implements RowMapper<T> {

    private final Class<T> mappedClass;
    private final Constructor<?> constructor;
    private final Map<String, Integer> parameterIndexMap;

    public ColumnMatchingRowMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.constructor = findWidestConstructor();
        this.parameterIndexMap = buildParameterIndexMap(constructor);
    }

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
        if (constructor == null) return Map.of();
        Parameter[] parameters = constructor.getParameters();
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            indexMap.put(parameters[i].getName(), i);
        }
        return indexMap;
    }

    private T mapUsingParameterizedConstructor(ResultSet rs) throws SQLException {
        try {
            Object[] args = resolveConstructorArguments(rs);
            return (T) constructor.newInstance(args);
        } catch (Exception e) {
            throw new SQLException("생성자를 통한 객체 매핑 실패", e);
        }
    }

    private Object[] resolveConstructorArguments(ResultSet rs) throws SQLException {
        Object[] args = new Object[constructor.getParameterCount()];
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        for (int i = 1; i <= columnCount; i++) {
            String label = NamingUtils.snakeToCamel(rsmd.getColumnLabel(i).toLowerCase());
            Integer index = parameterIndexMap.get(label);
            if (index != null) {
                Object value = rs.getObject(i);
                Class<?> parameterType = parameterTypes[index];
                args[index] = TypeConversionService.convert(value, parameterType);
            }
        }
        return args;
    }

    private T mapUsingFieldInjection(ResultSet rs) throws SQLException {
        try {
            T mappedObject = mappedClass.getDeclaredConstructor().newInstance();
            populateFieldsFromResultSet(mappedObject, rs);
            return mappedObject;
        } catch (Exception e) {
            throw new SQLException("기본 생성자 기반 객체 매핑 실패", e);
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
            Field field = mappedClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object convertedValue = TypeConversionService.convert(value, field.getType());
            field.set(target, convertedValue);
        } catch (NoSuchFieldException ignored) {
        } catch (Exception e) {
            throw new RuntimeException("필드 설정 실패: " + fieldName, e);
        }
    }
}
