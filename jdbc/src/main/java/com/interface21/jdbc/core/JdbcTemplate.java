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

    public int update(final String sql, final Object... args) {
        return executeQuery(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> findAll(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(sql, pstmt -> {
            try (final var rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        }, args);
    }

    public <T> T find(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(sql, pstmt -> {
            try (final var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            }
        }, args);
    }

    private <T> T executeQuery(
            final String sql,
            final PreparedStatementCallback<T> callback,
            final Object... args
    ) {
        try (final var connection = dataSource.getConnection();
             final var pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            final PreparedStatementSetter setter = createPreparedStatementSetter(args);
            setter.setValues(pstmt);

            return callback.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T execute(PreparedStatement pstmt) throws SQLException;
    }
}
