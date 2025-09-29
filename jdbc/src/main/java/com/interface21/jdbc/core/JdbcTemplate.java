package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int executeUpdate(final String sql, final Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < parameters.length; i++) {
                final Object parameter = parameters[i];
                if (parameter instanceof String) {
                    pstmt.setString(i + 1, (String) parameter);
                }
                if (parameter instanceof Long) {
                    pstmt.setLong(i + 1, (Long) parameter);
                }
            }

            return pstmt.executeUpdate();
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

    public <T> T executeQueryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < parameters.length; i++) {
                final Object parameter = parameters[i];
                if (parameter instanceof String) {
                    pstmt.setString(i + 1, (String) parameter);
                }
                if (parameter instanceof Long) {
                    pstmt.setLong(i + 1, (Long) parameter);
                }
            }

            final ResultSet rs = pstmt.executeQuery();

            return rowMapper.mapForObject(rs);
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

    public <T> List<T> executeQuery(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < parameters.length; i++) {
                final Object parameter = parameters[i];
                if (parameter instanceof String) {
                    pstmt.setString(i + 1, (String) parameter);
                }
                if (parameter instanceof Long) {
                    pstmt.setLong(i + 1, (Long) parameter);
                }
            }

            final ResultSet rs = pstmt.executeQuery();
            return rowMapper.mapForObjects(rs);
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
}
