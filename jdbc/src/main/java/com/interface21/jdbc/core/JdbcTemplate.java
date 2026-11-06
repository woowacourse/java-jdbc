package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DuplicateKeyException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JdbcTemplate {
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public int update(final String sql, final Object... args) {
        return update(sql, statement -> bind(statement, args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, statement -> bind(statement, args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                           final RowMapper<T> rowMapper) {
        return execute(sql, setter, queryCallback(rowMapper));
    }

    /** The caller owns the connection and its transaction. */
    public int update(final Connection connection, final String sql, final Object... args) {
        return execute(connection, sql, statement -> bind(statement, args), PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(final Connection connection, final String sql,
                               final RowMapper<T> rowMapper, final Object... args) {
        return singleResult(execute(connection, sql, statement -> bind(statement, args), queryCallback(rowMapper)));
    }

    private <T> StatementCallback<List<T>> queryCallback(final RowMapper<T> rowMapper) {
        return statement -> {
            try (var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rowNum++));
                }
                return results;
            }
        };
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final StatementCallback<T> callback) {
        try (var connection = dataSource.getConnection()) {
            return execute(connection, sql, setter, callback);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to obtain or close connection", e);
        }
    }

    private <T> T execute(final Connection connection, final String sql,
                          final PreparedStatementSetter setter, final StatementCallback<T> callback) {
        try (var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return callback.execute(statement);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateKeyException("Duplicate key while executing SQL: " + sql, e);
            }
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {
        T execute(PreparedStatement statement) throws SQLException;
    }

    /** Returns null when no row matches; rejects ambiguous multiple-row results. */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return singleResult(query(sql, rowMapper, args));
    }

    private <T> T singleResult(final List<T> results) {
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row, but found " + results.size());
        }
        return results.isEmpty() ? null : results.getFirst();
    }

    private void bind(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
