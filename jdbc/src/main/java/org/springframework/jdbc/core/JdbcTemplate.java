package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... params) {
        preparedStatementExecutor.execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return preparedStatementExecutor.execute(sql, pstmt -> {
            final ResultSet rs = pstmt.executeQuery();
            final List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return results;
        }, params);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return preparedStatementExecutor.execute(sql, pstmt -> {
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rowMapper.mapRow(rs, rs.getRow()));
            }
            return Optional.empty();
        }, params);
    }
}
