package com.interface21.jdbc.core.extractor;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ReflectiveExtractor<T> extends ResultSetExtractor<T> {
    private final Class<T> clazz;

    public ReflectiveExtractor(ResultSet resultSet, Class<T> clazz) {
        super(resultSet);
        this.clazz = clazz;
    }

    @Override
    public T extractOne() throws SQLException {
        try {
            T instance = clazz.getConstructor().newInstance();
            injectValues(resultSet, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new SQLException(e);
        }
    }

    private void injectValues(ResultSet resultSet, T instance) throws SQLException, IllegalAccessException {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Object value = resultSet.getObject(declaredField.getName());
            declaredField.setAccessible(true);
            declaredField.set(instance, value);
        }
    }
}
