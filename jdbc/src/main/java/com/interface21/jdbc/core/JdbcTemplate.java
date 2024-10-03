package com.interface21.jdbc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int executeUpdate(String sql, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, parameters);
            return statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForObject(String sql, Class<T> clazz, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, parameters);
            ResultSet resultSet = statement.executeQuery();
            List<T> result = new ArrayList<>();

            while (resultSet.next()) {
                T instance = createNewInstance(clazz);
                setFields(instance, resultSet);
                result.add(instance);
            }

            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(PreparedStatement statement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    private <T> T createNewInstance(Class<T> clazz) throws Exception {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private <T> void setFields(T instance, ResultSet resultSet) throws Exception {
        Field[] fields = instance.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Object object = resultSet.getObject(i + 1, TypeConverterUtils.convertToWrapperIfPrimitive(field.getType()));
            field.setAccessible(true);
            field.set(instance, object);
        }
    }
}
