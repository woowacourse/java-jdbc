package com.interface21.jdbc.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlResultSetMapper {

    private static final Logger log = LoggerFactory.getLogger(SqlResultSetMapper.class);

    private SqlResultSetMapper() {
    }

    public static <T> List<T> doQueryMapping(Class<T> clazz, ResultSet resultSet)
            throws SQLException {
        if (resultSet.isAfterLast()) {
            return null;
        }

        List<T> queryResult = new ArrayList<>();

        while (resultSet.next()) {
            T newInstance = createInstanceFromResultSet(clazz, resultSet);
            queryResult.add(newInstance);
        }
        log.info("queryResult = {}", queryResult);

        return queryResult;
    }

    private static <T> T createInstanceFromResultSet(Class<T> clazz, ResultSet resultSet)
            throws SQLException {
        try {
            T newInstance = clazz.getConstructor().newInstance();
            Field[] declaredFields = clazz.getDeclaredFields();

            for (Field declaredField : declaredFields) {
                Object value = resultSet.getObject(declaredField.getName());
                declaredField.setAccessible(true);
                declaredField.set(newInstance, value);
            }

            return newInstance;
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new SQLException(e);
        }
    }

}
