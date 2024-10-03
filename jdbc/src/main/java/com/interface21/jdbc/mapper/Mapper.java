package com.interface21.jdbc.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Mapper {

    public static <T> T queryResolver(Class<T> clazz, String sql, ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return null;
        }

        try {
            T instance = clazz.getConstructor().newInstance();

            int from = sql.indexOf("from");
            List<String> fieldNames = Arrays.stream(sql.substring(6, from).trim().split(","))
                    .map(String::trim)
                    .toList();

            while (rs.next()) {
                for (String fieldName : fieldNames) {
                    Field field = clazz.getDeclaredField(fieldName);
                    Object resultSetValue = rs.getObject(getLableName(fieldName));

                    System.out.println("@@" + field.getName() + "@@");
                    field.setAccessible(true);
                    field.set(instance, resultSetValue);
                    field.setAccessible(false);
                }
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getLableName(String s) {
        StringBuilder sb = new StringBuilder(s);
        for (int i = 1; i < sb.length(); i++) {
            if (Character.isUpperCase(sb.charAt(i))) {
                sb.insert(i, "_");
            }
        }
        System.out.println("@@" + sb.toString() + "@@");
        return sb.toString().toLowerCase();
    }

}
