package nextstep.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BeanPropertyRowMapper<T> implements RowMapper<T> {

    private final Class<T> type;
    private final Field[] fields;

    public BeanPropertyRowMapper(final Class<T> type) {
        this.type = type;
        fields = type.getDeclaredFields();
    }

    @Override
    public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        T instance = instantiate();
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            String fieldName = field.getName();
            Object fieldValue = rs.getObject(fieldName, fieldType);
            setField(instance, field, fieldValue);
        }
        return instance;
    }

    private T instantiate() {
        try {
            Constructor<T> primaryConstructor = getPrimaryConstructor();
            return primaryConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Constructor<T> getPrimaryConstructor() {
        try {
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("BeanPropertyRowMapper needs a primary constructor!", e);
        }
    }

    private void setField(final T instance, final Field field, final Object fieldValue) throws SQLException {
        try {
            field.set(instance, fieldValue);
        } catch (IllegalAccessException e) {
            throw new SQLException(e);
        }
    }
}
