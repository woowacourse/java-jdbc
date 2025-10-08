package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... params) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            bindParams(pstmt, params);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(String sql, Function<ResultSet, T> rowMapper, Object... params) {
        final List<T> results = query(sql, rowMapper, params);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    public <T> List<T> query(String sql, Function<ResultSet, T> rowMapper, Object... params) {
        final var result = new ArrayList<T>();
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            bindParams(pstmt, params);
            try (final var rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rowMapper.apply(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void bindParams(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
