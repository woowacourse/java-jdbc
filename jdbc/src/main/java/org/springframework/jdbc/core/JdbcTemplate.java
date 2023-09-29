package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void execute(String sql) {
        context(new PreparedStrategy() {
            @Override
            public PreparedStatement createStatement(Connection connection) throws SQLException {
                return connection.prepareStatement(sql);
            }
        });
    }

    public void execute(String sql, Object... args) {
        context(new PreparedStrategy() {
            @Override
            public PreparedStatement createStatement(Connection connection) throws SQLException {
                return connection.prepareStatement(sql);
            }
        }, args);
    }

    public void context(PreparedStrategy preparedStrategy) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = preparedStrategy.createStatement(conn);
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

    public void context(PreparedStrategy preparedStrategy, Object[] args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = preparedStrategy.createStatement(conn);

            for (int i = 0; i < args.length; i++) {
                pstmt.setString(i + 1, args[i].toString());
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
