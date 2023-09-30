package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, args);
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("SQL exception occurred!");
            throw new RuntimeException(e);
        }
    }

    public List<Object> query(final String sql, final Class<?> clazz) {
        ResultSet resultSet = null;

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            resultSet = statement.executeQuery();
            final List<Object> instances = new ArrayList<>();

            while (resultSet.next()) {
                final Object instance = instantiateFromResultSet(clazz, resultSet);
                instances.add(instance);
            }
            return instances;

        } catch (SQLException e) {
            log.error("SQL exception occurred!");
            throw new RuntimeException(e);
        } finally {
            closeResultSet(resultSet);
        }
    }

    // TODO: 프록시로 Connection, Statement 획득하는 로직 분리

    public Optional<Object> queryForObject(final String sql, final Class<?> clazz, final Object... args) {
        ResultSet resultSet = null;

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, args);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final Object instance = instantiateFromResultSet(clazz, resultSet);
                return Optional.of(instance);
            }
            return Optional.empty();

        } catch (SQLException e) {
            log.error("SQL exception occurred!");
            throw new RuntimeException(e);
        } finally {
            closeResultSet(resultSet);
        }
    }

    private Object instantiateFromResultSet(final Class<?> clazz, final ResultSet resultSet) throws SQLException {
        try {
            final List<Object> columns = extractColumns(resultSet);

            final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            final Constructor<?> compatibleConstructor = Arrays.stream(constructors)
                    .filter(constructor -> isCompatible(constructor, columns))
                    .findFirst()
                    .orElseThrow();

            return compatibleConstructor.newInstance(columns.toArray());

        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            log.error("Reflection exception occurred!");
            throw new RuntimeException(e);
        }
    }

    private List<Object> extractColumns(final ResultSet resultSet) throws SQLException {
        final List<Object> columns = new ArrayList<>();

        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            final Object column = resultSet.getObject(i + 1);
            columns.add(column);
        }
        return columns;
    }

    private boolean isCompatible(final Constructor<?> constructor, final List<Object> columns) {
        final List<String> constructorTypeNames = Arrays.stream(constructor.getParameterTypes())
                .map(Class::getSimpleName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        final List<String> columnTypeNames = columns.stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return constructorTypeNames.equals(columnTypeNames);
    }

    private void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("failed to close ResultSet!");
                throw new RuntimeException(e);
            }
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
