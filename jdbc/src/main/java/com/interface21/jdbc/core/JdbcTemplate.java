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

    public void update(final String sql, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParams(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            close(pstmt);
            close(conn);
        }
    }

    public <T> T queryForObject(final String sql, final ResultsetMapper<T> mapper, final Object... args) {
        List<T> results = query(sql, mapper, args);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new RuntimeException("결과값이 1개보다 많습니다. 결과 크기: " + results.size());
        }
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final ResultsetMapper<T> mapper, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParams(pstmt, args);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            close(rs);
            close(pstmt);
            close(conn);
        }
    }

    private void setParams(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private static void close(final Connection conn) {
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignored) {
        }
    }

    private static void close(final PreparedStatement pstmt) {
        try {
            if (pstmt != null) pstmt.close();
        } catch (SQLException ignored) {
        }
    }

    private static void close(final ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException ignored) {
        }
    }
}
