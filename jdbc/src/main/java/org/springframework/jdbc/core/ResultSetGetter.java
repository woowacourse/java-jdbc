package org.springframework.jdbc.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.jdbc.core.JdbcTemplateException.EntityReflectionException;

public class ResultSetGetter<T> {

    private final Map<String, SqlType> values;
    private final Class<T> classForObject;

    public ResultSetGetter(final Map<String, Class<?>> values, final Class<T> classForObject) {
        this.values = new HashMap<>();
        values.forEach((key, value) -> this.values.put(key, SqlType.get(value)));
        this.classForObject = classForObject;
    }

    public T getObject(final ResultSet rs) throws SQLException {
        try {
            final T instance = classForObject.getConstructor().newInstance();
            setField(rs, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchFieldException e) {
            throw new EntityReflectionException(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    private void setField(final ResultSet rs, final T instance) throws SQLException, NoSuchFieldException, IllegalAccessException {
        for (final Entry<String, SqlType> entry : values.entrySet()) {
            final Object value = getValue(rs, entry.getValue(), entry.getKey());
            final Field field = instance.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(instance, value);
        }
    }

    private Object getValue(final ResultSet rs, final SqlType sqlType, final String field) throws SQLException {
        if (sqlType == SqlType.STRING) {
            return rs.getString(field);
        }
        if (sqlType == SqlType.BOOLEAN) {
            return rs.getBoolean(field);
        }
        if (sqlType == SqlType.INT) {
            return rs.getInt(field);
        }
        if (sqlType == SqlType.LONG) {
            return rs.getLong(field);
        }
        return null;
    }
}
