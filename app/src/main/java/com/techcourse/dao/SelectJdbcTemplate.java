package com.techcourse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SelectJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    protected abstract String createQuery();

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    protected abstract DataSource getDataSource();

    protected abstract Object mapRow(ResultSet rs) throws SQLException;

    public Object query() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(createQuery());
            setValues(pstmt);
            rs = executeQuery(pstmt);

            log.debug("query : {}", createQuery());

            return mapRow(rs);
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

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;
}
