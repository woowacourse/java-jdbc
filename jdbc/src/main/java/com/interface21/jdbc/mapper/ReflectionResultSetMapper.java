package com.interface21.jdbc.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Arrays;

public class ReflectionResultSetMapper implements Mapper {

    // Reflection을 활용한 객체 매핑.
    // 클래스의 필드 순서와 생성자의 파라미터 순서가 다르면 작동 안함.
    @Override
    public <T> T map(ResultSet rs, Class<T> clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        Class<?>[] fieldTypes = Arrays.stream(fields)
                .map(Field::getType)
                .toArray(Class<?>[]::new);

        Constructor<T> constructor = findCompatibleConstructor(clazz, fieldTypes);
        Object[] constructorArgs = new Object[fields.length];

        for (int i = 0; i < fields.length; i++) {
            constructorArgs[i] = rs.getObject(i + 1);
        }

        return constructor.newInstance(constructorArgs);
    }

    private static <T> Constructor<T> findCompatibleConstructor(Class<T> clazz, Class<?>[] fieldTypes) {
        // 클래스의 모든 생성자를 가져온다.
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            Class<?>[] constructorParamTypes = constructor.getParameterTypes();

            // 파라미터 개수가 필드 개수와 다르면 건너뛴다.
            if (constructorParamTypes.length != fieldTypes.length) {
                continue;
            }

            // 파라미터 타입과 필드 타입이 순서대로 호환되는지 검사한다.
            if (areTypesCompatible(constructorParamTypes, fieldTypes)) {
                return (Constructor<T>) constructor;
            }
        }

        throw new IllegalStateException("필드와 호환되는 생성자를 찾을 수 없습니다: " + Arrays.toString(fieldTypes));
    }

    /**
     * 두 타입 배열이 순서대로 호환되는지 검사합니다.
     */
    private static boolean areTypesCompatible(Class<?>[] paramTypes, Class<?>[] fieldTypes) {
        for (int i = 0; i < paramTypes.length; i++) {
            if (!TypeUtils.isCompatible(paramTypes[i], fieldTypes[i])) {
                return false; // 하나라도 호환되지 않으면 실패
            }
        }
        return true; // 모두 호환되면 성공
    }
}
