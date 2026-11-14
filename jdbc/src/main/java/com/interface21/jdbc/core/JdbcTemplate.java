package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
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

    public int update(final String sql, final Object... arguments) {
        return update(sql, createSetter(arguments));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(sql, rowMapper, statement -> {});
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                           final Object... arguments) {
        return query(sql, rowMapper, createSetter(arguments));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                           final PreparedStatementSetter setter) {
        return execute(sql, setter, statement -> {
            try (final var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                               final Object... arguments) {
        return queryForObject(sql, rowMapper, createSetter(arguments));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                               final PreparedStatementSetter setter) {
        final var results = query(sql, rowMapper, setter);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row, found " + results.size()
                    + " for SQL: " + sql);
        }
        return results.isEmpty() ? null : results.get(0);
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final StatementCallback<T> callback) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);
            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private static PreparedStatementSetter createSetter(final Object[] arguments) {
        final Object[] values = arguments == null ? new Object[0] : arguments;
        return statement -> {
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
        };
    }

    @FunctionalInterface
    private interface StatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
