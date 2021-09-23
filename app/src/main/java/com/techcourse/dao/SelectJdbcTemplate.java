package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SelectJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    private final DataSource dataSource;

    public SelectJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object query(String sql) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParams(pstmt);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (!rs.next()) {
                return null;
            }
            return mapFromRow(rs);
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

    abstract void setParams(PreparedStatement pstmt) throws SQLException;

    abstract Object mapFromRow(ResultSet rs) throws SQLException;
}
