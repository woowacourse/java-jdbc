package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i+1, args[i]);
            }

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {}
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<T> list = new ArrayList<>();

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query: {}", sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i+1, args[i]);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(rowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {}

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {}
        }

        return list;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query: {}", sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i+1, args[i]);
            }

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {}

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {}
        }
    }
}
