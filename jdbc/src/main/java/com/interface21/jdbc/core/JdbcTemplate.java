package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        execute(sql, preparedStatementSetter, PreparedStatement::executeUpdate);
    }

    public void update(final String sql, final Object... parameters) {
        update(sql, createPreparedStatementSetter(parameters));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                             final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatementSetter, preparedStatement -> {
            try (final var resultSet = preparedStatement.executeQuery()) {
                final var results = new ArrayList<T>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, rowMapper, createPreparedStatementSetter(parameters));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                                final PreparedStatementSetter preparedStatementSetter) {
        final var results = query(sql, rowMapper, preparedStatementSetter);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return queryForObject(sql, rowMapper, createPreparedStatementSetter(parameters));
    }

    private <R> R execute(final String sql, final PreparedStatementSetter preparedStatementSetter,
                          final JdbcOperation<R> operation) {
        log.debug("query : {}", sql);
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setParameters(preparedStatement);
            return operation.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... parameters) {
        return preparedStatement -> {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
        };
    }

    @FunctionalInterface
    private interface JdbcOperation<R> {

        R execute(PreparedStatement preparedStatement) throws SQLException;
    }
}
