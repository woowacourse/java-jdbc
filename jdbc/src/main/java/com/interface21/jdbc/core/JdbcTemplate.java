package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, args, PreparedStatement::executeUpdate);
    }

    public int update(final Connection connection, final String sql, final Object... args) {
        return execute(connection, sql, args, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, args, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                final var results = new ArrayList<T>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, args, statement -> queryForObject(statement, rowMapper));
    }

    public <T> T queryForObject(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                                final Object... args) {
        return execute(connection, sql, args, statement -> queryForObject(statement, rowMapper));
    }

    private <T> T queryForObject(final PreparedStatement statement, final RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return null;
            }

            final var result = rowMapper.mapRow(resultSet);
            if (resultSet.next()) {
                throw new DataAccessException("Expected at most one row, but found multiple rows");
            }
            return result;
        }
    }

    private <T> T execute(final String sql, final Object[] args, final StatementCallback<T> callback) {
        try (Connection connection = getConnection()) {
            return execute(connection, sql, args, callback);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final Connection connection, final String sql, final Object[] args,
                          final StatementCallback<T> callback) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(statement, args);
            return callback.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {
        T execute(PreparedStatement statement) throws SQLException;
    }
}
