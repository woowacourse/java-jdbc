package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SelectJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract Object mapRow(ResultSet rs) throws SQLException;

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    public Object query() {
        final String sql = createQuery();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);
            setValues(pstmt);
            rs = executeQuery(pstmt);
            log.debug("query : {}", sql);
            return mapRow(rs);
        } catch (final SQLException e) {
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
}
