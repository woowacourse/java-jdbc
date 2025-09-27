package com.interface21.jdbc.core;

import com.interface21.jdbc.rowmapper.RowMapper;
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

    public void update(String sql, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        Object[] objects = parameters;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("update : {}", sql);

            for (int i = 1; i <= objects.length; i++) {
                pstmt.setObject(i, objects[i - 1]);
            }
            pstmt.executeUpdate();
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

    public <T> T queryForObject(String sql, RowMapper rowMapper, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 1; i <= parameters.length; i++) {
                pstmt.setObject(i, parameters[i - 1]);
            }
            rs = pstmt.executeQuery();
            log.debug("queryForObject : {}", sql);
            if (rs.next()) {
                return rowMapper.rowMap(rs, 1);
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

    public <T> List<T> query(String sql, RowMapper rowMapper, Object... parameters) {
        // todo
        final var results = new ArrayList<T>();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 1; i <= parameters.length; i++) {
                pstmt.setObject(i, parameters[i - 1]);
            }
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            while (rs.next()) {
                results.add(rowMapper.rowMap(rs, 1));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }
}
