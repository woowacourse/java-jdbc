package com.interface21.jdbc.core;

import java.sql.Connection;
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

    public int update(final String sql, final Object... args) {
        return execute((conn, pstmt, rs) -> {
                    conn = dataSource.getConnection();
                    pstmt = conn.prepareStatement(sql);
                    setParameters(args, pstmt);

                    log.debug("query : {}", sql);

                    return pstmt.executeUpdate();
                }
        );
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute((conn, pstmt, rs) -> {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParameters(args, pstmt);

            log.debug("query : {}", sql);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowMapper.mapRow(rs, 0);
            }
            return null;
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute((conn, pstmt, rs) -> {
                    conn = dataSource.getConnection();
                    pstmt = conn.prepareStatement(sql);
                    setParameters(args, pstmt);

                    log.debug("query : {}", sql);

                    rs = pstmt.executeQuery();
                    final List<T> result = new ArrayList<>();
                    while (rs.next()) {
                        final T object = rowMapper.mapRow(rs, 0);
                        result.add(object);
                    }
                    return result;
                }
        );
    }

    private void setParameters(final Object[] args, final PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            final Object arg = args[i];
            if (arg instanceof Long) {
                pstmt.setLong(i + 1, (long) arg);
            }
            if (arg instanceof String) {
                pstmt.setString(i + 1, (String) arg);
            }
        }
    }

    private <T> T execute(final Executor<T> executor) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            return executor.execute(conn, pstmt, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    @FunctionalInterface
    private interface Executor<T> {

        T execute(Connection connection, PreparedStatement pstmt, ResultSet rs) throws SQLException;
    }
}
