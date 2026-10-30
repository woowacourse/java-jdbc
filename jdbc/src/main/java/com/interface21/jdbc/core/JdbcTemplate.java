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

    public int update(final String sql, final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, statement -> {
            try (final ResultSet resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        }, args);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new DataAccessException("단건 조회 결과가 여러 건입니다: " + results.size());
        }
        return Optional.ofNullable(results.get(0));
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, args);
            return callback.doInPreparedStatement(statement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T doInPreparedStatement(final PreparedStatement statement) throws SQLException;
    }
}
