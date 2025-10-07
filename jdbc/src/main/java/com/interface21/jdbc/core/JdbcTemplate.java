package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
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

    @FunctionalInterface
    private interface StatementCallback<T> {
        T doInPreparedStatement(PreparedStatement pstmt) throws SQLException;
    }

    private <T> T execute(final String sql, final StatementCallback<T> action, final Object... args) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    pstmt.setObject(i + 1, args[i]);
                }
            }
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            try (final var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            }
        }, args);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            try (final var rs = pstmt.executeQuery()) {
                final List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }
        }, args);
    }
}
