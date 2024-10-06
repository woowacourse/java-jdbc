package com.interface21.jdbc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Arrays;

public class RowMapperFactory {

    public static <T> RowMapper<T> getRowMapper(Class<T> clazz) {
        return (rs) -> {
            try {
                Constructor<?> constructor = findAllargsConstructor(clazz);
                constructor.setAccessible(true);
                Object[] constructorArguments = getConstructorArguments(constructor, clazz, rs);
                return (T) constructor.newInstance(constructorArguments);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        };
    }

    private static <T> Constructor<?> findAllargsConstructor(Class<T> clazz) throws NoSuchMethodException {
        int fieldCount = clazz.getDeclaredFields().length;
        return Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == fieldCount)
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException("모든 필드를 포함한 생성자를 찾을 수 없습니다. class: " + clazz.getName()));
    }

    private static Object[] getConstructorArguments(Constructor<?> constructor, Class<?> clazz, ResultSet rs)
            throws Exception {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Field[] fields = clazz.getDeclaredFields();
        Object[] initargs = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            String fieldName = fields[i].getName();
            initargs[i] = rs.getObject(fieldName);
        }
        return initargs;
    }
}