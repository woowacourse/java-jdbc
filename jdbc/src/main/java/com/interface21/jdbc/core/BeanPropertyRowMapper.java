package com.interface21.jdbc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.annotation.Nullable;

public class BeanPropertyRowMapper<T> implements RowMapper<T> {

    private final Class<T> aClass;

    public BeanPropertyRowMapper(Class<T> aClass) {
        this.aClass = aClass;
    }

    @Nullable
    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            Constructor<T> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T bean = constructor.newInstance();
            Arrays.stream(aClass.getDeclaredFields())
                    .forEach(field -> setField(rs, field, bean));
            return bean;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void setField(ResultSet rs, Field field, T bean) {
        try {
            field.setAccessible(true);
            field.set(bean, rs.getObject(field.getName()));
        } catch (IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
