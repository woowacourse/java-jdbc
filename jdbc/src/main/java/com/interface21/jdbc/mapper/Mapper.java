package com.interface21.jdbc.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Mapper {

    private Mapper() {
    }

    public static <T> List<T> queryResolver(Class<T> clazz, String sqlStatement, ResultSet resultSet)
            throws SQLException {
        if (resultSet.isAfterLast()) {
            return null;
        }

        try {
            List<String> fieldNames = getFieldNameFrom(sqlStatement);
            final List<T> queryResult = new ArrayList<>();

            while (resultSet.next()) {
                T newInstance = createInstanceFromResultSet(clazz, resultSet, fieldNames);
                queryResult.add(newInstance);
            }

            return queryResult;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T createInstanceFromResultSet(Class<T> clazz, ResultSet resultSet, List<String> fieldNames)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException, SQLException {
        final T instance = clazz.getConstructor().newInstance();

        for (String fieldName : fieldNames) {
            Field field = clazz.getDeclaredField(fieldName);

            String columnName = toColumnName(fieldName);
            Object columnValue = resultSet.getObject(columnName);

            setFieldValueToInstance(instance, field, columnValue);
        }

        return instance;
    }

    private static <T> void setFieldValueToInstance(T instance, Field field, Object columnValue)
            throws IllegalAccessException {
        field.setAccessible(true);
        field.set(instance, columnValue);
        field.setAccessible(false);
    }

    private static List<String> getFieldNameFrom(String sqlStatement) {
        int selectIndex = sqlStatement.indexOf("select") + "select".length();
        int fromIndex = sqlStatement.indexOf("from");
        String arguments = sqlStatement.substring(selectIndex, fromIndex).trim();

        return Arrays.stream(arguments.split(","))
                .map(String::trim)
                .toList();
    }

    private static String toColumnName(String fieldName) {
        StringBuilder builder = new StringBuilder(fieldName);

        IntStream.range(1, builder.length())
                .filter(index -> Character.isUpperCase(builder.charAt(index)))
                .forEach(index -> builder.insert(index, "_"));

        return builder.toString().toLowerCase();
    }
}
