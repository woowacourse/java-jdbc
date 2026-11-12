package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {
    private final DataSource dataSource;
    private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... parameters) {
        return updateWithSetter(sql, statement -> bind(statement, parameters));
    }

    public int updateWithSetter(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> mapper, final Object... parameters) {
        return queryWithSetter(sql, mapper, statement -> bind(statement, parameters));
    }

    public <T> List<T> queryWithSetter(final String sql, final RowMapper<T> mapper,
                                       final PreparedStatementSetter setter) {
        return execute(sql, setter, statement -> {
            try (var resultSet = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> mapper, final Object... parameters) {
        List<T> results = query(sql, mapper, parameters);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row but found " + results.size());
        }
        return results.isEmpty() ? null : results.get(0);
    }

    /** Runs operations on this template on one connection; nested transactions are not supported. */
    public void inTransaction(final Runnable action) {
        if (transactionConnection.get() != null) {
            throw new IllegalStateException("Nested transactions are not supported");
        }
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            transactionConnection.set(connection);
            try {
                action.run();
                connection.commit();
            } catch (RuntimeException | Error | SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw e;
            } finally {
                transactionConnection.remove();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute transaction", e);
        }
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final StatementCallback<T> callback) {
        Connection existing = transactionConnection.get();
        if (existing != null) {
            return execute(existing, sql, setter, callback);
        }
        try (var connection = dataSource.getConnection()) {
            return execute(connection, sql, setter, callback);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private <T> T execute(final Connection connection, final String sql, final PreparedStatementSetter setter,
                          final StatementCallback<T> callback) {
        try (var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private void bind(final PreparedStatement statement, final Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {
        T execute(PreparedStatement statement) throws SQLException;
    }
}
