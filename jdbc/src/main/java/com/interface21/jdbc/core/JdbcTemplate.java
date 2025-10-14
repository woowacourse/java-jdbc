package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long updateAndReturnKey(final String sql, final Object... args) {
        return execute(
                sql,
                (conn, s) -> conn.prepareStatement(s, Statement.RETURN_GENERATED_KEYS),
                pstmt -> {
                    pstmt.executeUpdate();
                    try (var resultSet = pstmt.getGeneratedKeys()) {
                        if (resultSet.next()) {
                            return resultSet.getLong(1);
                        }
                        throw new DataAccessException("No generated key returned for query: " + sql);
                    }
                },
                args
        );
    }

    public void update(final String sql, final Object... args) {
        int updated = execute(sql, Connection::prepareStatement, PreparedStatement::executeUpdate, args);
        if (updated == 0) {
            throw new DataAccessException("No rows affected for query: " + sql);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new DataAccessException("Expected 1 result, got " + results.size() + " for query: " + sql);
        }
        return results.getFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(
                sql,
                Connection::prepareStatement,
                pstmt -> {
                    try (var resultSet = pstmt.executeQuery()) {
                        var results = new ArrayList<T>();
                        while (resultSet.next()) {
                            results.add(rowMapper.mapRow(resultSet));
                        }
                        return results;
                    }
                },
                args
        );
    }

    private <R> R execute(
            final String sql,
            final PreparedStatementFactory factory,
            final SqlExecutor<R> executor,
            final Object... args
    ) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try (var pstmt = factory.create(connection, sql)) {
            PreparedStatementSetter.of(args)
                    .setParameters(pstmt);
            return executor.apply(pstmt);
        } catch (final SQLException e) {
            throw new DataAccessException("SQL failed: " + sql, e);
        }
    }
}
