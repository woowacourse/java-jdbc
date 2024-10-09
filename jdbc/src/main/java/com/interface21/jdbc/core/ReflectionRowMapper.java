package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ReflectionRowMapper<T> implements RowMapper<T> {

    private final Class<T> clazz;

    public ReflectionRowMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T mapRow(ResultSet rs) {
        Field[] fields = clazz.getDeclaredFields();
        Object[] arguments = getArgumentsFromResultSet(rs, fields);
        Class<?>[] fieldTypes = getFieldTypes(fields);
        try {
            return clazz.getConstructor(fieldTypes).newInstance(arguments);
        } catch (ReflectiveOperationException e) {
            throw new DataAccessException(e);
        }
    }

    private Object[] getArgumentsFromResultSet(ResultSet rs, Field[] fields) {
        return Arrays.stream(fields)
                .map(Field::getName)
                .map(name -> name.replaceAll("([a-z])([A-Z])", "$1_$2"))
                .map(String::toLowerCase)
                .map(name -> getObject(rs, name))
                .toArray();
    }

    private Object getObject(ResultSet rs, String name) {
        try {
            return rs.getObject(name);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Class<?>[] getFieldTypes(Field[] fields) {
        return Arrays.stream(fields)
                .map(Field::getType)
                .toArray(Class<?>[]::new);
    }
}
