package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
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

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public int update(final String sql, final Object... parameters) {
        return update(sql, createPreparedStatementSetter(parameters));
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                            final RowMapper<T> mapper) {
        return execute(sql, setter, statement -> {
            try (final var resultSet = statement.executeQuery()) {
                final var results = new ArrayList<T>();
                while (resultSet.next()) {
                    results.add(mapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> mapper, final Object... parameters) {
        return query(sql, createPreparedStatementSetter(parameters), mapper);
    }

    /**
     * 조회 결과가 없으면 null, 여러 행이면 첫 번째 행을 반환한다.
     */
    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter,
                               final RowMapper<T> mapper) {
        return execute(sql, setter, statement -> {
            try (final var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapper.mapRow(resultSet);
                }
                return null;
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> mapper, final Object... parameters) {
        return queryForObject(sql, createPreparedStatementSetter(parameters), mapper);
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        log.debug("query : {}", sql);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return callback.doInPreparedStatement(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... parameters) {
        return statement -> {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
        };
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T doInPreparedStatement(PreparedStatement statement) throws SQLException;
    }
}
