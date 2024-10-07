package com.interface21.jdbc.core;

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

    public void update(String sql, Parameters parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            parameters.setPreparedStatement(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Cannot access database and connection or invalid sql query : " + sql);
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

    public <T> T queryForObject(String sql, Parameters parameters, RowMapper<T> rowMapper) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            parameters.setPreparedStatement(pstmt);

            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            if (rs.next()) {
                return (T) rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Cannot access database and connection or invalid sql query : " + sql);
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

    public <T> List<T> query (String sql, Parameters parameters, RowMapper<T> rowMapper) {
    public <T> List<T> query(String sql, Parameters parameters, RowMapper<T> rowMapper) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            parameters.setPreparedStatement(pstmt);

            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            List<T> objects = new ArrayList<>();
            while (rs.next()) {
                T object = rowMapper.mapRow(rs);
                objects.add(object);
            }
            return objects;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Cannot access database and connection or invalid sql query : " + sql);
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
}
