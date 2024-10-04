package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public void update(final String sql, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeStatement(pstmt);
            releaseConnection(conn);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            List<T> results = new ArrayList<>();

            if (rs.next()) {
                results.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            releaseConnection(conn);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return rowMapper.mapRow(rs, rs.getRow());
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            releaseConnection(conn);
        }
    }

    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {}
    }

    private void closeStatement(PreparedStatement pstmt) {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException ignored) {}
    }

    private void releaseConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ignored) {}
    }
}
