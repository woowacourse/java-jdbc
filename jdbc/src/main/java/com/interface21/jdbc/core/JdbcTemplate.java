package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        return execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(rowMapper.mapRow(resultSet));
                }
                return rows;
            }
        }, params);
    }

    public <T> Optional<T> queryFirst(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(rowMapper.mapRow(resultSet));
                }
                return Optional.empty();
            }
        }, params);
    }

    private <T> T execute(final String sql, final StatementCallback<T> callback, final Object[] params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
