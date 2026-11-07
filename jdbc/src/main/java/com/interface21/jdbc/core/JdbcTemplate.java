package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes JDBC operations and translates SQL exceptions.
 * Overloads accepting a Connection leave its transaction and lifecycle to the caller;
 * only statements and result sets created by the template are closed.
 */
public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return update(sql, createPreparedStatementSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public int update(final Connection connection, final String sql, final Object... args) {
        return update(connection, sql, createPreparedStatementSetter(args));
    }

    public int update(final Connection connection, final String sql, final PreparedStatementSetter setter) {
        return execute(connection, sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return execute(sql, setter, statement -> mapRows(statement, rowMapper));
    }

    public <T> List<T> query(final Connection connection, final String sql,
                           final RowMapper<T> rowMapper, final Object... args) {
        return query(connection, sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(final Connection connection, final String sql,
                           final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return execute(connection, sql, setter, statement -> mapRows(statement, rowMapper));
    }

    /**
     * Returns the mapped row, or {@code null} if no row matches.
     *
     * @throws DataAccessException if more than one row matches or a JDBC operation fails
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, createPreparedStatementSetter(args), rowMapper);
    }

    /**
     * Returns the mapped row using custom parameter binding, or {@code null} if no row matches.
     *
     * @throws DataAccessException if more than one row matches or a JDBC operation fails
     */
    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return singleResult(query(sql, setter, rowMapper));
    }

    public <T> T queryForObject(final Connection connection, final String sql,
                               final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(connection, sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> T queryForObject(final Connection connection, final String sql,
                               final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return singleResult(query(connection, sql, setter, rowMapper));
    }

    private <T> T singleResult(final List<T> results) {
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row but found " + results.size());
        }
        return results.getFirst();
    }

    private <T> List<T> mapRows(final PreparedStatement statement, final RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        try (Connection connection = dataSource.getConnection()) {
            return executeStatement(connection, sql, setter, callback);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private <T> T execute(final Connection connection, final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        try {
            return executeStatement(connection, sql, setter, callback);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private <T> T executeStatement(final Connection connection, final String sql,
                                   final PreparedStatementSetter setter,
                                   final PreparedStatementCallback<T> callback) throws SQLException {
        log.debug("query : {}", sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return callback.doInPreparedStatement(statement);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... args) {
        return statement -> {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
        };
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T doInPreparedStatement(PreparedStatement statement) throws SQLException;
    }
}
