package com.interface21.jdbc.core;

import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.dao.SqlExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (final var connection = dataSource.getConnection()) {
            update(sql, connection, args);
        } catch (SQLException e) {
            throw new SqlExecutionException(e);
        }
    }

    public void update(final String sql, final Connection connection, final Object... args) {
        try (final var pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlExecutionException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        var results = query(sql, rowMapper, args);
        return getSingleResult(results);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                                final Connection connection, final Object... args) {
        var results = query(sql, connection, rowMapper, args);
        return getSingleResult(results);
    }

    private <T> T getSingleResult(List<T> results) {
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException();
        }
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final var connection = dataSource.getConnection()) {
            return query(sql, connection, rowMapper, args);
        } catch (SQLException e) {
            throw new SqlExecutionException(e);
        }
    }

    public <T> List<T> query(final String sql, final Connection connection,
                             final RowMapper<T> rowMapper, final Object... args) {
        try (final var pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            try (final var resultSet = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                var rowNum = 0;

                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rowNum++));
                }

                return results;
            }
        } catch (SQLException e) {
            throw new SqlExecutionException(e);
        }
    }
}