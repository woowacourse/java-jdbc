package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Deprecated
    public int update(String sql, Object... params) {
        return update(sql, (pstmt) -> bindParams(pstmt, params));
    }

    public int update(String sql, PreparedStatementSetter setter) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.accept(pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Deprecated
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return query(sql, (pstmt) -> bindParams(pstmt, params), rowMapper);
    }

    public <T> List<T> query(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) {
        final List<T> result = new ArrayList<>();
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            try (final var rs = executeQuery(pstmt, setter)) {
                while (rs.next()) {
                    result.add(rowMapper.apply(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Deprecated
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(sql, (pstmt) -> bindParams(pstmt, params), rowMapper);
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            try (final var rs = executeQuery(pstmt, setter)) {
                if (rs.next()) {
                    return rowMapper.apply(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt, PreparedStatementSetter setter) {
        try {
            setter.accept(pstmt);
            return pstmt.executeQuery();
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
