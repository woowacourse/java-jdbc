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

    public <T> T select(final String sql, final RowMapper<T> callback, final Object... values) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return callback.call(rs);
                }
                return null;
            }
        }, values);
    }

    public <T> List<T> selectList(final String sql, final RowMapper<T> callback, final Object... values) {
        return execute(sql, pstmt -> {
            final List<T> results = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    final T call = callback.call(rs);
                    results.add(call);
                }
            }
            return results;
        }, values);
    }

    public void update(final String sql, final Object... values) {
        execute(sql, PreparedStatement::executeUpdate, values);
    }

    private <T> T execute(String sql, JdbcCallback<T> callback, Object... values) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            return callback.call(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
