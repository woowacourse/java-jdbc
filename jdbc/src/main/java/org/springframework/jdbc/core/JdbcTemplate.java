package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, MyPreparedStatementCallback myPreparedStatementCallback) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();

            log.info("query: {}", sql);
            pstmt = conn.prepareStatement(sql);

            myPreparedStatementCallback.execute(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            close(conn, pstmt, null);
        }
    }

    public <T> List<T> queryAll(String sql, MyRowMapper<T> rowMapper, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            log.info("query: {}", sql);
            pstmt = conn.prepareStatement(sql);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setObject(i, params[i - 1]);
            }
            rs = pstmt.executeQuery();

            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rowMapper.map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public <T> T queryForObject(String sql, MyRowMapper<T> rowMapper, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            log.info("query: {}", sql);
            pstmt = conn.prepareStatement(sql);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setObject(i, params[i - 1]);
            }
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rowMapper.map(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }

    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {}

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
