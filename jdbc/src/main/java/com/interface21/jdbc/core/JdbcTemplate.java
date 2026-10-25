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
        return execute(sql, preparedStatement -> mapRows(preparedStatement, rowMapper), args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.getFirst();
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

    private <T> List<T> mapRows(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper)
            throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException;
    }
}
