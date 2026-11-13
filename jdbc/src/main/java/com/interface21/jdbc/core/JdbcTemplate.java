package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
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
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public int update(final Connection connection, final String sql, final Object... args) {
        return execute(connection, sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final var results = query(sql, rowMapper, args);
        return getSingleResult(results);
    }

    public <T> T queryForObject(
            final Connection connection,
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        final var results = execute(connection, sql, createQueryCallback(rowMapper), args);
        return getSingleResult(results);
    }

    private <T> T getSingleResult(final List<T> results) {
        if (results.size() != 1) {
            throw new DataAccessException("Expected exactly 1 row but found " + results.size());
        }
        return results.getFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, createQueryCallback(rowMapper), args);
    }

    private <T> PreparedStatementCallback<List<T>> createQueryCallback(final RowMapper<T> rowMapper) {
        return pstmt -> {
            try (final var rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        };
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementCallback<T> callback,
            final Object... args
    ) {
        try (final var connection = dataSource.getConnection()) {
            return execute(connection, sql, callback, args);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(
            final Connection connection,
            final String sql,
            final PreparedStatementCallback<T> callback,
            final Object... args
    ) {
        try (final var pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            final PreparedStatementSetter setter = createPreparedStatementSetter(args);
            setter.setValues(pstmt);

            return callback.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
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
