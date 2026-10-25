package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        }, args);
    }

    /**
     * 조회 결과가 없으면 null을 반환하고, 두 건 이상이면 DataAccessException을 던진다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                final T result = rowMapper.mapRow(resultSet);
                if (resultSet.next()) {
                    throw new DataAccessException("Expected at most one row, but found more than one row");
                }
                return result;
            }
        }, args);
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            bindParameters(statement, args);
            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private void bindParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
