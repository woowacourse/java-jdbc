package com.interface21.jdbc.core;

import com.interface21.jdbc.ObjectMapper;
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


    public <T> T query(ObjectMapper<T> objectMapper, String sql, Object... param) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            setStatement(conn, pstmt, sql, param);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return objectMapper.mapToObject(rs);
            }
            throw new IllegalStateException("Fail to get result set"); //TODO: 예외 구체화

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            closeConnection(conn, pstmt, rs);
            throw new RuntimeException(e);

        } finally {
            closeConnection(conn, pstmt, rs);
        }
    }

    public <T> List<T> queryList(ObjectMapper<T> objectMapper, String sql, Object... param) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            setStatement(conn, pstmt, sql, param);
            rs = pstmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(objectMapper.mapToObject(rs));
            }
            return results;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            closeConnection(conn, pstmt, rs);
            throw new RuntimeException(e);

        } finally {
            closeConnection(conn, pstmt, rs);
        }
    }

    public void execute(String sql, Object... param) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            setStatement(conn, pstmt, sql, param);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            closeConnection(conn, pstmt, rs);
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, pstmt, rs);
        }
    }

    private void setStatement(Connection conn, PreparedStatement pstmt, String sql, Object[] param) throws SQLException {
        conn = dataSource.getConnection();
        pstmt = conn.prepareStatement(sql);

        for (int i = 0; i < param.length; i++) {
            pstmt.setObject(i + 1, param[i]);
        }
    }

    private void closeConnection(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
        } catch (SQLException ignored) {
        }

        try {
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
        } catch (SQLException ignored) {
        }

        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException ignored) {
        }
    }
}
