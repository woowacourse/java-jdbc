package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int index = 1; index < parameters.length + 1; index++) {
                pstmt.setObject(index, parameters[index - 1]);
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

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int index = 1; index < parameters.length + 1; index++) {
                pstmt.setObject(index, parameters[index - 1]);
            }
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);
            if (rs.next()) {
                return rowMapper.map(rs);
            }
            throw new NoSuchElementException("조건에 맞는 데이터가 없습니다.");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int index = 1; index < parameters.length + 1; index++) {
                pstmt.setObject(index, parameters[index - 1]);
            }
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);
            List<T> results = new ArrayList<>();
            if (rs.next()) {
                results.add(rowMapper.map(rs));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
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
