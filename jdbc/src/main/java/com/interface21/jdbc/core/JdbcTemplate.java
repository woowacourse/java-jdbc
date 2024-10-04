package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String query, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(query);
            setParameters(pstmt, parameters);

            log.debug("query : {}", query);

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

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void setParameters(PreparedStatement pstmt, Object parameter, int index) throws SQLException {
        Class<?> parameterClass = parameter.getClass();
        if (parameterClass.equals(String.class)) {
            pstmt.setString(index, (String) parameter);
        }
        if (parameterClass.equals(Long.class)) {
            pstmt.setLong(index, (long) parameter);
        }
    }
    private void setParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            setParameter(pstmt, parameters[i-1], i);
        }
    }
}
