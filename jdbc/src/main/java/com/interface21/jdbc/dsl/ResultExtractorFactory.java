package com.interface21.jdbc.dsl;

import com.interface21.jdbc.core.ResultExtractor;
import java.lang.reflect.Field;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ResultExtractorFactory {

    public static <T> ResultExtractor<T> create(Class<T> targetClass) {
        return resultSet -> {
            try {
                T instance = targetClass.getDeclaredConstructor().newInstance();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = resultSet.getObject(i);

                    try {
                        Field field = targetClass.getDeclaredField(columnName);
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
}
