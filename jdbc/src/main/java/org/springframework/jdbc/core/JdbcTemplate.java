package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... params) {
        preparedStatementExecutor.execute(sql, preparedStatement -> {
            try {
                return preparedStatement.executeUpdate();
            } catch (final SQLException e) {
                throw new DataAccessException(e);
            }
        }, params);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return preparedStatementExecutor.execute(sql, pstmt -> {
            try {
                final ResultSet rs = pstmt.executeQuery();
                final List<T> results = new ArrayList<>();

                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rs.getRow()));
                }
                return results;
            } catch (final SQLException e) {
                throw new DataAccessException(e);
            }
        }, params);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return preparedStatementExecutor.execute(sql, pstmt -> {
            try {
                final ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(rowMapper.mapRow(rs, rs.getRow()));
                }
                return Optional.empty();
            } catch (final SQLException e) {
                throw new DataAccessException(e);
            }
        }, params);
    }
}
