package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        update(sql, ps -> setParameters(ps, args));
    }

    public void update(final Connection connection, final String sql, final Object... args) {
        update(connection, sql, ps -> setParameters(ps, args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, ps -> setParameters(ps, args));
    }

    public <T> List<T> query(final Connection connection, final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(connection, sql, rowMapper, ps -> setParameters(ps, args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    public <T> T queryForObject(final Connection connection, final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(connection, sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    private void update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void update(final Connection connection, final String sql, final PreparedStatementSetter preparedStatementSetter) {
        try (final var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter preparedStatementSetter) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            return executeQuery(preparedStatement, rowMapper, preparedStatementSetter);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> query(final Connection connection, final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter preparedStatementSetter) {
        try (final var preparedStatement = connection.prepareStatement(sql)) {
            return executeQuery(preparedStatement, rowMapper, preparedStatementSetter);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> executeQuery(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper, final PreparedStatementSetter preparedStatementSetter) throws SQLException {
        preparedStatementSetter.setValues(preparedStatement);
        try (final var resultSet = preparedStatement.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object... args) throws SQLException {
        validateParameterCount(preparedStatement, args.length);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    private void validateParameterCount(final PreparedStatement preparedStatement, final int actualCount) throws SQLException {
        final int expectedCount = preparedStatement.getParameterMetaData().getParameterCount();
        if (expectedCount != actualCount) {
            throw new IllegalArgumentException(
                    String.format("SQL parameter count mismatch: expected %d but was %d", expectedCount, actualCount)
            );
        }
    }
}
