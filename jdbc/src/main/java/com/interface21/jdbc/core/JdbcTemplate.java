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

    public void update(final String sql, Object[] args) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            setObject(args, pstmt);
            pstmt.executeUpdate();

            log.debug("query : {}", sql);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
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

    public <T> List<T> query(final String sql, Object[] args, RowMapper<T> rowMapper) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<T> results = new ArrayList<>();

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            if (args != null) {
                setObject(args, pstmt);
            }
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }

            return results;

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

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper) {
        return query(sql, null, rowMapper);
    }

    public <T> T queryForObject(final String sql, Object[] args, RowMapper<T> rowMapper) {
        List<T> results = query(sql, args, rowMapper);

        if (results.size() != 1) {
            throw new IllegalStateException("queryForObject 결과가 1개가 아닙니다.");
        }

        return results.getFirst();
    }

    private void setObject(Object[] args, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
