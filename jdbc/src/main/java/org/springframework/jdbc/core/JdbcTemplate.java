package org.springframework.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

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

                    return calculateResult(rowMapper, rs);
                }, sql, args
        );
    }

    private <T> T calculateResult(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rowMapper.mapRow(rs, rs.getRow());
        }

        return null;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return preparedStatementExecutor.execute(
                pstmt -> {
                    final ResultSet rs = pstmt.executeQuery();

                    return getResults(rowMapper, rs);
                }, sql
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
}
