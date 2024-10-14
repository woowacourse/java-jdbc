package com.interface21.jdbc.core.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructorRowMapper<T> implements RowMapper<T> {

    private static final Logger log = LoggerFactory.getLogger(ConstructorRowMapper.class);

    private final Class<T> clazz;

    public ConstructorRowMapper(final Class<T> clazz) {
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
            setFieldValue(resultSet, instance, field);
        }
    }

    private void setFieldValue(
            final ResultSet resultSet,
            final T instance,
            final Field field
    ) throws SQLException, IllegalAccessException {
        field.setAccessible(true);
        field.set(instance, resultSet.getObject(field.getName()));
    }
}
