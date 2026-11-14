package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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

    /**
     * 단일 null 값은 (Object) null 또는 new Object[]{null}로 전달해 setter 오버로드와 구분한다.
     */
    public int update(final String sql, final Object... parameters) {
        return update(sql, createSetter(parameters));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public int update(final Connection connection, final String sql, final Object... parameters) {
        return update(connection, sql, createSetter(parameters));
    }

    public int update(final Connection connection, final String sql, final PreparedStatementSetter setter) {
        return execute(connection, sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, createSetter(parameters), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return execute(sql, setter, statement -> mapRows(statement, rowMapper));
    }

    public <T> List<T> query(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                           final Object... parameters) {
        return query(connection, sql, createSetter(parameters), rowMapper);
    }

    public <T> List<T> query(final Connection connection, final String sql, final PreparedStatementSetter setter,
                           final RowMapper<T> rowMapper) {
        return execute(connection, sql, setter, statement -> mapRows(statement, rowMapper));
    }

    /**
     * 조회 결과가 없으면 null을 반환하고, 두 건 이상이면 IncorrectResultSizeDataAccessException을 던진다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return queryForObject(sql, createSetter(parameters), rowMapper);
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return singleResult(sql, query(sql, setter, rowMapper));
    }

    public <T> T queryForObject(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                                final Object... parameters) {
        return queryForObject(connection, sql, createSetter(parameters), rowMapper);
    }

    public <T> T queryForObject(final Connection connection, final String sql, final PreparedStatementSetter setter,
                                final RowMapper<T> rowMapper) {
        return singleResult(sql, query(connection, sql, setter, rowMapper));
    }

    private <T> List<T> mapRows(final PreparedStatement statement, final RowMapper<T> rowMapper) throws SQLException {
        try (final var resultSet = statement.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }
    }

    private <T> T singleResult(final String sql, final List<T> results) {
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("Expected at most one row but found " + results.size() + ": " + sql);
        }
        return results.isEmpty() ? null : results.getFirst();
    }

    private PreparedStatementSetter createSetter(final Object[] parameters) {
        return statement -> {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
        };
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter, final StatementCallback<T> callback) {
        final Connection connection;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
        } catch (final CannotGetJdbcConnectionException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e.getCause());
        }

        Throwable failure = null;
        try {
            return execute(connection, sql, setter, callback);
        } catch (final RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (final CannotGetJdbcConnectionException e) {
                if (failure == null) {
                    throw new DataAccessException("Failed to execute SQL: " + sql, e.getCause());
                }
                failure.addSuppressed(e.getCause());
            } catch (final RuntimeException | Error e) {
                if (failure == null) {
                    throw e;
                }
                failure.addSuppressed(e);
            }
        }
    }

    // 전달받은 Connection의 트랜잭션과 종료는 호출자가 관리한다.
    private <T> T execute(final Connection connection, final String sql, final PreparedStatementSetter setter,
                          final StatementCallback<T> callback) {
        log.debug("query : {}", sql);
        try (final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return callback.execute(statement);
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
