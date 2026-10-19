package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            bind(statement, args);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute JDBC update", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            bind(statement, args);
            try (var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, results.size()));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute JDBC query", e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final var results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new IllegalStateException("Expected at most one row but found " + results.size());
        }
        return results.isEmpty() ? null : results.getFirst();
    }

    private void bind(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
