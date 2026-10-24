package com.techcourse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update(final String sql, final PreparedStatementSetter pss) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            pss.setValues(pstmt);
            pstmt.executeUpdate();
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

    public Object query(final String sql, final PreparedStatementSetter pss, final RowMapper rowMapper) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = executeQuery(pss, pstmt);

            log.debug("query : {}", sql);

            return rowMapper.mapRow(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
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

    private ResultSet executeQuery(final PreparedStatementSetter pss, final PreparedStatement pstmt) throws SQLException {
        pss.setValues(pstmt);
        return pstmt.executeQuery();
    }

    protected abstract DataSource getDataSource();
}
