package org.springframework.jdbc.core.rowmapper;

import org.springframework.dao.DataAccessException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class RowMapperGenerator {

    private RowMapperGenerator() {
    }

    public static <T> RowMapper<T> generateRowMapperOf(Class<T> requiredType) {
        return rs -> {
            T instance = createInstance(requiredType);
            fillFields(requiredType, rs, instance);
            return instance;
        };
    }

    private static <T> T createInstance(Class<T> requiredType) {
        try {
            Constructor<T> constructor = requiredType.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            constructor.setAccessible(false);
            return instance;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void fillFields(Class<T> requiredType, ResultSet rs, T instance) {
        Field[] fields = requiredType.getDeclaredFields();
        for (final Field field : fields) {
            final String fieldName = field.getName();
            try {
                for (int i = 0; i < fields.length; i++) {
                    final String columnName = rs.getMetaData().getColumnName(i + 1);
                    if (columnName.equalsIgnoreCase(fieldName)) {
                        field.setAccessible(true);
                        field.set(instance, rs.getObject(i + 1));
                        field.setAccessible(false);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }
}
