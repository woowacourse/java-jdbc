package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
        return update(sql, argumentSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, argumentSetter(args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                             final RowMapper<T> rowMapper) {
        return execute(sql, setter, rowsCallback(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, argumentSetter(args), rowMapper);
    }

    <T> T queryForObject(final String sql, final PreparedStatementSetter setter,
                         final RowMapper<T> rowMapper) {
        final var results = query(sql, setter, rowMapper);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row, but found " + results.size());
        }
        return results.isEmpty() ? null : results.get(0);
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            return execute(connection, sql, setter, callback);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private <T> T execute(final Connection connection, final String sql,
                          final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        try (final var statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);
            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private <T> PreparedStatementCallback<List<T>> rowsCallback(final RowMapper<T> rowMapper) {
        return statement -> {
            try (final var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        };
    }

    private PreparedStatementSetter argumentSetter(final Object[] args) {
        return statement -> {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
        };
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
