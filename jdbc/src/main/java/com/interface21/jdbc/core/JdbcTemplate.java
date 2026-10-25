package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(sql, resultSet -> mapRows(resultSet, rowMapper), args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(sql, resultSet -> mapSingleRow(resultSet, rowMapper), args);
    }

    private <T> T executeQuery(final String sql, final ResultSetCallback<T> callback, final Object... args) {
        return execute(sql, preparedStatement -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return callback.doInResultSet(resultSet);
            }
        }, args);
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback, final Object... args) {
        log.debug("query : {}", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setArguments(preparedStatement, args);
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다: " + sql, e);
        }
    }

    private void setArguments(final PreparedStatement preparedStatement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
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
