package com.interface21.jdbc.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mapper {

    private static final Logger log = LoggerFactory.getLogger(Mapper.class);

    private Mapper() {
    }

    public static <T> List<T> doQueryMapping(Class<T> clazz, String sqlStatement, ResultSet resultSet)
            throws SQLException {
        if (resultSet.isAfterLast()) {
            return null;
        }

        List<String> fieldNames = getFieldNameFrom(sqlStatement);
        final List<T> queryResult = new ArrayList<>();

        while (resultSet.next()) {
            T newInstance = createInstanceFromResultSet(clazz, resultSet, fieldNames);
            queryResult.add(newInstance);
        }

        log.info("queryResult = {}", queryResult);

        return queryResult;
    }

    private static <T> T createInstanceFromResultSet(Class<T> clazz, ResultSet resultSet, List<String> fieldNames)
            throws SQLException {
        try {
            final T newInstance = instantiate(clazz);

            for (String fieldName : fieldNames) {
                Field field = clazz.getDeclaredField(fieldName);

                String columnName = toColumnName(fieldName);
                Object columnValue = resultSet.getObject(columnName);

                setFieldValueToInstance(newInstance, field, columnValue);
            }
            return newInstance;
        } catch (IllegalAccessException | NoSuchFieldException | InstantiationException e) {
            throw new SQLException(e);
        }
    }

    private static <T> T instantiate(Class<T> clazz) throws InstantiationException {
        try {
            return clazz.getConstructor().newInstance();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InstantiationException();
        }
    }

    private static <T> void setFieldValueToInstance(T instance, Field field, Object columnValue)
            throws IllegalAccessException {
        field.setAccessible(true);
        field.set(instance, columnValue);
        field.setAccessible(false);
    }

    private static List<String> getFieldNameFrom(String sqlStatement) throws SQLException {
        try {
            int selectIndex = sqlStatement.indexOf("select") + "select".length();
            int fromIndex = sqlStatement.indexOf("from");
            String arguments = sqlStatement.substring(selectIndex, fromIndex).trim();

            return Arrays.stream(arguments.split(","))
                    .map(String::trim)
                    .toList();
        } catch (IndexOutOfBoundsException e) {
            throw new SQLException(e);
        }
    }

    private static String toColumnName(String fieldName) {
        StringBuilder builder = new StringBuilder(fieldName);

        IntStream.range(1, builder.length())
                .filter(index -> Character.isUpperCase(builder.charAt(index)))
                .forEach(index -> builder.insert(index, "_"));

        return builder.toString().toLowerCase();
    }
}
