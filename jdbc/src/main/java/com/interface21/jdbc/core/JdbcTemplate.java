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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return update(sql, statement -> bindParameters(statement, args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        try {
            return execute(sql, setter, PreparedStatement::executeUpdate);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute update: " + sql, e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, statement -> bindParameters(statement, args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        try {
            return execute(sql, setter, statement -> readRows(statement, rowMapper));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query: " + sql, e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row, but got " + results.size());
        }
        return results.isEmpty() ? null : results.getFirst();
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) throws SQLException {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (ConnectionRelease release = () -> DataSourceUtils.releaseConnection(connection, dataSource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);
            return callback.doInPreparedStatement(statement);
        }
    }

    @FunctionalInterface
    private interface ConnectionRelease extends AutoCloseable {
        @Override
        void close();
    }

    private <T> List<T> readRows(final PreparedStatement statement, final RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, results.size()));
            }
            return results;
        }
    }

    private void bindParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T doInPreparedStatement(PreparedStatement statement) throws SQLException;
    }
}
