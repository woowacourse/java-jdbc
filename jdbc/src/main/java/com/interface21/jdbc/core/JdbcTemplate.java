package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, ps -> {
            setPreparedStatementParams(params, ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            }
        });
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, ps -> {
            setPreparedStatementParams(params, ps);
            return extractList(rowMapper, ps);
        });
    }

    public int update(final String sql, final Object... params) {
        return execute(sql, ps -> {
            setPreparedStatementParams(params, ps);
            return ps.executeUpdate();
        });
    }

    private <T> List<T> extractList(RowMapper<T> rowMapper, PreparedStatement ps)
            throws SQLException {
        List<T> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
        }
        return results;
    }

    private void setPreparedStatementParams(final Object[] params, final PreparedStatement ps) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    //https://github.com/spring-projects/spring-framework/blob/6aeb9d16e83c69d980f48a4a4f052bf52f31dfd0/spring-jdbc/src/main/java/org/springframework/jdbc/core/JdbcTemplate.java#L654
    private <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        final var conn = DataSourceUtils.getConnection(dataSource);
        try (final var ps = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Jdbc Data Access Failed", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
