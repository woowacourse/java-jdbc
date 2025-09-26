package com.interface21.jdbc.dsl;

import com.interface21.jdbc.core.ResultExtractor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Locale;

public class ResultExtractorFactory {

    public static <T> ResultExtractor<T> create(Class<T> targetClass) {
        return resultSet -> {
            try {
                Constructor<T> noArgsConstructor = targetClass.getDeclaredConstructor();
                noArgsConstructor.setAccessible(true);
                T instance = noArgsConstructor.newInstance();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);

                    try {
                        Field field = targetClass.getDeclaredField(snakeToCamel(columnName));
                        field.setAccessible(true);
                        field.set(instance, value);
                    } catch (NoSuchFieldException ignore) {
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new SQLException("Failed to map ResultSet to " + targetClass.getName(), e);
            }
        };
    }

    private static String snakeToCamel(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.indexOf('_') < 0) return lower;
        StringBuilder sb = new StringBuilder();
        boolean up = false;
        for (char c : lower.toCharArray()) {
            if (c == '_') {
                up = true;
            } else if (up) {
                sb.append(Character.toUpperCase(c));
                up = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
