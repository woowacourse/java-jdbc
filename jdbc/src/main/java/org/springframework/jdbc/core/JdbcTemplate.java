package org.springframework.jdbc.core;

import org.springframework.jdbc.core.exception.EmptyResultDataAccessException;
import org.springframework.jdbc.core.exception.IncorrectResultSizeDataAccessException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    public static final int MAX_RESULT_SIZE = 1;
    public static final int FIRST_INDEX = 0;

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public int update(final String sql, final Object... args) {
        return preparedStatementExecutor.execute(PreparedStatement::executeUpdate, sql, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return preparedStatementExecutor.execute(
                pstmt -> {
                    final ResultSet rs = pstmt.executeQuery();
                    final List<T> results = getResults(rowMapper, rs);

                    return getSingleResult(results);
                }, sql, args
        );
    }

    private <T> List<T> getResults(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> results = new ArrayList<>();

        while (rs.next()) {
            final T result = calculateResult(rowMapper, rs);
            results.add(result);
        }

        return results;
    }

    private <T> T calculateResult(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rowMapper.mapRow(rs, rs.getRow());
        }

        return null;
    }

    private <T> T getSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException("조회된 결과가 존재하지 않습니다.");
        }
        if (results.size() > MAX_RESULT_SIZE) {
            throw new IncorrectResultSizeDataAccessException("조회된 결과의 개수가 적절하지 않습니다.");
        }

        return results.get(FIRST_INDEX);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return preparedStatementExecutor.execute(
                pstmt -> {
                    final ResultSet rs = pstmt.executeQuery();

                    return getResults(rowMapper, rs);
                }, sql
        );
    }
}
