package org.springframework.jdbc.core;

import org.springframework.dao.RowMapper;
import org.springframework.exception.EmptyResultException;
import org.springframework.exception.WrongResultSizeException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final int SINGLE_RESULT_SIZE = 1;

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public int update(final String sql, final Object... parameters) {
        return preparedStatementExecutor.execute(
                PreparedStatement::executeUpdate,
                sql,
                parameters
        );
    }

    public int update(final Connection connection, final String sql, final Object... parameters) {
        return preparedStatementExecutor.execute(
                connection,
                PreparedStatement::executeUpdate,
                sql,
                parameters
        );
    }

    public <T> List<T> query(final RowMapper<T> rowMapper, final String sql, final Object... parameters) {
        return preparedStatementExecutor.execute(
                preparedStatement -> findQueryResults(rowMapper, preparedStatement),
                sql,
                parameters
        );
    }

    private <T> List<T> findQueryResults(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        final ResultSet rs = pstmt.executeQuery();

        final List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }

    public <T> T queryForObject(final RowMapper<T> rowMapper, final String sql, final Object... parameters) {
        final List<T> result = query(rowMapper, sql, parameters);

        validateResultSize(result.size());
        return result.get(0);
    }

    private void validateResultSize(final int size) {
        if (size > SINGLE_RESULT_SIZE) {
            throw new WrongResultSizeException("Result Count is Not Only 1. ResultCount=" + size);
        }
        if (size == 0) {
            throw new EmptyResultException("Result is Empty");
        }
    }
}
