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

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    protected abstract DataSource getDataSource();

    public void update(String sql, PreparedStatementSetter setter) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);

            setter.setValues(pstmt);
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
        }
    }

    public Object query(String sql, PreparedStatementSetter setter, RowMapper rowMapper) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);
            setter.setValues(pstmt);
            rs = executeQuery(pstmt);

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
            } catch (SQLException ignored) {
            }
        }
    }
}
