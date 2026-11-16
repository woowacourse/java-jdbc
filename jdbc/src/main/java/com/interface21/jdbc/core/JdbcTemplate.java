package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return update(sql, statement -> setParameters(statement, args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, statement -> setParameters(statement, args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return execute(sql, setter, statement -> executeQuery(statement, rowMapper));
    }

    /** Returns null for no rows and throws DataAccessException for multiple rows. */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return singleResult(query(sql, rowMapper, args));
    }

    private <T> List<T> executeQuery(final PreparedStatement statement, final RowMapper<T> rowMapper)
            throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return mapRows(resultSet, rowMapper);
        }
    }

    /** Uses the connection bound to the current transaction if any, and closes only connections it opened. */
    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            return execute(connection, sql, setter, callback);
        } catch (SQLException e) {
            failure = e;
            throw new DataAccessException(e);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            release(connection, failure);
        }
    }

    private void release(final Connection connection, final Throwable failure) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (CannotGetJdbcConnectionException closeFailure) {
            if (failure == null) {
                throw closeFailure;
            }
            failure.addSuppressed(closeFailure.getCause());
        }
    }

    private <T> T execute(final Connection connection, final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);
            return callback.doInPreparedStatement(statement);
        }
    }

    private <T> List<T> mapRows(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }

    private <T> T singleResult(final List<T> results) {
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row but found " + results.size());
        }
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    private void setParameters(final PreparedStatement statement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T doInPreparedStatement(PreparedStatement statement) throws SQLException;
    }
}