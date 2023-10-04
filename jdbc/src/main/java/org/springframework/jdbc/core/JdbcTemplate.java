package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        query(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args) {
        return query(sql, statement -> {
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapper.map(rs);
                }
                return null;
            }
        }, args);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... args) {
        return query(sql, statement -> {
            try (ResultSet rs = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
                return results;
            }
        }, args);
    }

    private <T> T query(
            final String sql,
            final JdbcCallback<T> jdbcCallback,
            final Object... args
    ) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = getPreparedStatement(conn, sql, args)) {
            log.debug("query : {}", sql);
            return jdbcCallback.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement getPreparedStatement(final Connection conn, final String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }
}
