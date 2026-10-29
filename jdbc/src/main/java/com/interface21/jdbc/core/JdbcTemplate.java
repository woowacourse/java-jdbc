package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatementSetter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        return execute(sql, preparedStatementSetter, queryCallback(resultSet -> mapRows(resultSet, rowMapper)));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        return execute(sql, preparedStatementSetter, queryCallback(resultSet -> mapSingleRow(resultSet, rowMapper)));
    }

    private <T> PreparedStatementCallback<T> queryCallback(final ResultSetCallback<T> callback) {
        return preparedStatement -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return callback.doInResultSet(resultSet);
            }
        };
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final PreparedStatementCallback<T> callback
    ) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            return execute(connection, sql, preparedStatementSetter, callback);
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

    private <T> T execute(
            final Connection connection,
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final PreparedStatementCallback<T> callback
    ) {
        log.debug("query : {}", sql);

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다: " + sql, e);
        }
    }

    private <T> List<T> mapRows(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private <T> T mapSingleRow(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        if (!resultSet.next()) {
            throw new IncorrectResultSizeDataAccessException("조회 결과가 없습니다.", 1);
        }
        final T result = rowMapper.mapRow(resultSet);
        if (resultSet.next()) {
            throw new IncorrectResultSizeDataAccessException("조회 결과가 2건 이상입니다.", 1);
        }
        return result;
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException;
    }

    @FunctionalInterface
    private interface ResultSetCallback<T> {

        T doInResultSet(ResultSet resultSet) throws SQLException;
    }
}
