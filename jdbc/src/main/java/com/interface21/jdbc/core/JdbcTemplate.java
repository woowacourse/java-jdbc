package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            bindParameters(statement, args);

            try (final var resultSet = statement.executeQuery()) {
                final var results = new ArrayList<T>();

                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }

                return List.copyOf(results);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args) {
        final var results = query(sql, rowMapper, args);

        if (results.isEmpty()) {
            return null;
        }

        return results.getFirst();
    }

    public int update(final String sql, final Object... args) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)
        ) {
            bindParameters(statement, args);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void bindParameters(
            final PreparedStatement statement,
            final Object[] args
            ) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
