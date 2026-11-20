package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        return execute(sql, PreparedStatement::executeUpdate, params);
    }

    public int update(final Connection connection, final String sql, final Object... params) {
        return execute(connection, sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(rowMapper.mapRow(resultSet));
                }
                return rows;
            }
        }, params);
    }

    public <T> Optional<T> queryFirst(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, singleResult(sql, rowMapper), params);
    }

    public <T> Optional<T> queryFirst(final Connection connection, final String sql,
                                    final RowMapper<T> rowMapper, final Object... params) {
        return execute(connection, sql, singleResult(sql, rowMapper), params);
    }

    private <T> StatementCallback<Optional<T>> singleResult(final String sql, final RowMapper<T> rowMapper) {
        return statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                T row = rowMapper.mapRow(resultSet);
                if (resultSet.next()) {
                    throw new DataAccessException("단건 조회 결과가 2개 이상입니다. SQL: " + sql);
                }
                return Optional.of(row);
            }
        };
    }

    private <T> T execute(final String sql, final StatementCallback<T> callback, final Object[] params) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            return execute(connection, sql, callback, params);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException e) {
                if (failure == null) {
                    throw e;
                }
                failure.addSuppressed(e);
            }
        }
    }

    private <T> T execute(final Connection connection, final String sql,
                          final StatementCallback<T> callback, final Object[] params) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다. SQL: " + sql, e);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
