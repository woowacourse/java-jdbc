package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(final String sql, final List<Object> parameters) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            for (int parameterIndex = 1; parameterIndex <= parameters.size(); parameterIndex++) {
                preparedStatement.setObject(parameterIndex, parameters.get(parameterIndex - 1));
            }

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("UPDATE query failed", e);
        }
    }

    public <T> Optional<T> executeSelect(final String sql, final EntityMapper<T> entityMapper, final List<Object> parameters) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            for (int parameterIndex = 1; parameterIndex <= parameters.size(); parameterIndex++) {
                preparedStatement.setObject(parameterIndex, parameters.get(parameterIndex - 1));
            }
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(entityMapper.map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("SELECT for multiple entities query failed", e);
        }

        return Optional.empty();
    }

    public <T> List<T> executeSelectAll(final String sql, final EntityMapper<T> entityMapper, final List<Object> parameters) {
        final List<T> result = new ArrayList<>();
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            for (int parameterIndex = 1; parameterIndex <= parameters.size(); parameterIndex++) {
                preparedStatement.setObject(parameterIndex, parameters.get(parameterIndex - 1));
            }
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(entityMapper.map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("SELECT for multiple entities query failed", e);
        }

        return result;
    }
}
