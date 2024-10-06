package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int PARAMETER_INDEX_OFFSET = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            for (int i = 0; i < parameters.length; i++) {
                setParameter(i + PARAMETER_INDEX_OFFSET, pstmt, parameters[i]);
            }
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

    private void setParameter(int parameterIndex, PreparedStatement pstmt, Object parameter) throws SQLException {
        if (parameter instanceof String) {
            pstmt.setString(parameterIndex, (String) parameter);
            return;
        }
        if (parameter instanceof Long) {
            pstmt.setLong(parameterIndex, (Long) parameter);
            return;
        }
        throw new IllegalArgumentException("준비되지 않은 파라미터 타입입니다. class = " + parameter.getClass().getCanonicalName());
    }

    // TODO: queryForObject() 구현

    // TODO: query() 구현
}
