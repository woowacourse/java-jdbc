package com.interface21.jdbc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

            for (int i = 1; i <= parameters.length; i++) {
                statement.setObject(i, parameters[i - 1]);
            }

            return statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> findOne(String sql, Class<T> clazz, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 1; i <= parameters.length; i++) {
                statement.setObject(i, parameters[i - 1]);
            }

            ResultSet resultSet = statement.executeQuery();

            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            Field[] fields = clazz.getDeclaredFields();

            if (resultSet.next()) {
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    Object object = resultSet.getObject(i + 1, PrimitiveTypeConverter.convert(field.getType()));
                    field.setAccessible(true);
                    field.set(instance, object);
                }

                return Optional.of(instance);
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> findAll(String sql, Class<T> clazz, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 1; i <= parameters.length; i++) {
                statement.setObject(i, parameters[i - 1]);
            }

            ResultSet resultSet = statement.executeQuery();
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);

            List<T> result = new ArrayList<>();

            while (resultSet.next()) {
                T instance = constructor.newInstance();
                Field[] fields = clazz.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    Object object = resultSet.getObject(i + 1, PrimitiveTypeConverter.convert(field.getType()));
                    field.setAccessible(true);
                    field.set(instance, object);
                }

                result.add(instance);
            }

            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
