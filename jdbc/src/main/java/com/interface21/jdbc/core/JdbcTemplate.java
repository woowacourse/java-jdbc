package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public <T> T select(final String sql, final JdbcCallback<T> callback, final Object... values) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= values.length; i++) {
                final Object value = values[i - 1];
                pstmt.setObject(i, value);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);
                if (rs.next()) {
                    return callback.call(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> selectList(final String sql, final JdbcCallback<T> callback, final Object... values) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= values.length; i++) {
                final Object value = values[i - 1];
                pstmt.setObject(i, value);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                if (rs.next()) {
                    final T call = callback.call(rs);
                    results.add(call);
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(final String sql, final Object... values) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 1; i <= values.length; i++) {
                final Object value = values[i - 1];
                pstmt.setObject(i, value);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
