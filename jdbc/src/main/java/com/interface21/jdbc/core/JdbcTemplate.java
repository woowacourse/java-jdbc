package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int MAX_ROWS_FOR_SINGLE_RESULT_CHECK = 2;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insert(final String sql, final Object... args) {
        return update(sql, args);
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    private <T> T execute(final String sql, final StatementCallback<T> callback, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            mapParameters(pstmt, args);
            return callback.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void mapParameters(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    public <R> R queryForObject(final String sql, final RowMapper<R> mapper, final Object... args) {
        return execute(sql, pstmt -> {
            pstmt.setMaxRows(MAX_ROWS_FOR_SINGLE_RESULT_CHECK);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("No result found.");
                }
                final R result = mapper.mapRow(resultSet);
                if (resultSet.next()) {
                    throw new IllegalStateException("Expected one result, but retrieved more than one.");
                }
                return result;
            }
        }, args);
    }

    public <R> List<R> query(final String sql, final RowMapper<R> mapper, final Object... args) {
        return execute(sql, pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                final List<R> tuples = new ArrayList<>();
                while (resultSet.next()) {
                    tuples.add(mapper.mapRow(resultSet));
                }
                return List.copyOf(tuples);
            }
        }, args);
    }

    @FunctionalInterface
    private interface StatementCallback<T> {
        T execute(PreparedStatement statement) throws SQLException;
    }
}
