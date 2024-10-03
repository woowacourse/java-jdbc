package com.interface21.jdbc.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mapper {

    public static <T> List<T> queryResolver(Class<T> clazz, String sql, ResultSet rs) throws SQLException {
        if (rs.isAfterLast()) {
            return null;
        }

        try {
            int from = sql.indexOf("from");
            List<String> fieldNames = Arrays.stream(sql.substring(6, from).trim().split(","))
                    .map(String::trim)
                    .toList();
            final List<T> result = new ArrayList<>();

            while (rs.next()) {
                T instance = clazz.getConstructor().newInstance();
                for (String fieldName : fieldNames) {
                    String columnName = toColumnName(fieldName);

                    Field field = clazz.getDeclaredField(fieldName);
                    Object columnValue = rs.getObject(columnName);

                    field.setAccessible(true);
                    field.set(instance, columnValue);
                    field.setAccessible(false);
                }
                result.add(instance);
            }
            for (var name : result) {
                System.out.println(name.toString());
            }

            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toColumnName(String s) {
        StringBuilder sb = new StringBuilder(s);
        for (int i = 1; i < sb.length(); i++) {
            if (Character.isUpperCase(sb.charAt(i))) {
                sb.insert(i, "_");
            }
        }
        return sb.toString().toLowerCase();
    }
}
