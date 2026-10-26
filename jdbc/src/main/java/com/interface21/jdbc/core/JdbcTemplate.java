package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public int update(final String sql, final Object... args) {
        return update(sql, argumentSetter(args));
    }

    private int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, argumentSetter(args), rowMapper);
    }

    private <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                             final RowMapper<T> rowMapper) {
        return execute(sql, setter, statement -> mapRows(statement, rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return singleResult(query(sql, rowMapper, args));
    }

    private <T> List<T> mapRows(final PreparedStatement statement, final RowMapper<T> rowMapper)
            throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, results.size()));
            }
            return results;
        }
    }

    private <T> T singleResult(final List<T> results) {
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row but found " + results.size());
        }
        return results.isEmpty() ? null : results.get(0);
    }

    private PreparedStatementSetter argumentSetter(final Object[] args) {
        return statement -> {
            for (int index = 0; index < args.length; index++) {
                statement.setObject(index + 1, args[index]);
            }
        };
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final StatementCallback<T> callback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            return execute(connection, sql, setter, callback);
        } catch (RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException closeFailure) {
                if (failure != null) {
                    failure.addSuppressed(closeFailure);
                } else {
                    throw closeFailure;
                }
            }
        }
    }

    private <T> T execute(final Connection connection, final String sql,
                          final PreparedStatementSetter setter, final StatementCallback<T> callback) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);
            return callback.execute(statement);
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to execute SQL: " + sql, exception);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {
        T execute(PreparedStatement statement) throws SQLException;
    }
}
