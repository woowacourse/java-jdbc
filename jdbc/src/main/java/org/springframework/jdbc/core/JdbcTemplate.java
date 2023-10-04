package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.IncorrectResultSizeException;
import org.springframework.dao.ResultEmptyException;

public class JdbcTemplate {

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... args) {
        preparedStatementExecutor.execute(sql, PreparedStatement::executeUpdate, args);
    }

    public void update(final Connection connection, final String sql, final Object... args) {
        preparedStatementExecutor.execute(connection, sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return preparedStatementExecutor.execute(sql, pstmt -> {
            ResultSet resultSet = pstmt.executeQuery();
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }
            return results;
        }, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return preparedStatementExecutor.execute(sql, pstmt -> {
            ResultSet resultSet = pstmt.executeQuery();
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }
            validateResultSize(results);
            return results.get(0);
        }, args);
    }

    private <T> void validateResultSize(final List<T> results) {
        if (results.isEmpty()) {
            throw new ResultEmptyException("result is empty.");
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeException(
                    String.format("Incorrect result size : expected - %d, actual - %d", 1, results.size()));
        }
    }
}
