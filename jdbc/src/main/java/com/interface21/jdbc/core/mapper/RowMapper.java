package com.interface21.jdbc.core.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowMapper<T> {

    private static final Logger log = LoggerFactory.getLogger(RowMapper.class);

    private final Class<T> clazz;

    public RowMapper(final Class<T> clazz) {
        this.clazz = clazz;
    }

    public T mapping(final ResultSet resultSet) {
        if (!checkResultSetHasData(resultSet)) {
            return null;
        }

        final T instance = createInstance();
        try {
            setFieldValues(instance, resultSet);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        return instance;
    }

    private boolean checkResultSetHasData(final ResultSet resultSet) {
        try {
            return resultSet.next();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    private T createInstance() {
        try {
            final Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private void setFieldValues(final T instance, final ResultSet resultSet)
            throws SQLException, IllegalAccessException {
        final Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            final Field field = fields[i];
            setFieldValue(resultSet, instance, field, i + 1);
        }
    }

    private void setFieldValue(
            final ResultSet resultSet,
            final T instance,
            final Field field,
            final int index
    ) throws SQLException, IllegalAccessException {
        final Class<?> fieldType = field.getType();
        field.setAccessible(true);
        if (fieldType.isAssignableFrom(String.class)) {
            final String value = resultSet.getString(index);
            field.set(instance, value);
            return;
        }

        final Long value = resultSet.getLong(index);
        field.set(instance, value);
    }
}
