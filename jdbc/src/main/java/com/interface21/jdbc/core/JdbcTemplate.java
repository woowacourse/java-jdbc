package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public void update(final String sql, final Object... args) {
        update(sql, createStatementSetter(args));
    }

    public void update(final String sql, final PreparedStatementSetter statementSetter) {
        try (final Connection connection = dataSource.getConnection()) {
            update(connection, sql, statementSetter);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute update", e);
        }
    }

    public void update(final Connection connection, final String sql, final Object... args) {
        update(connection, sql, createStatementSetter(args));
    }

    public void update(final Connection connection, final String sql,
                       final PreparedStatementSetter statementSetter) {
        log.debug("query : {}", sql);

        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            statementSetter.setValues(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute update", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, createStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter statementSetter,
                             final RowMapper<T> rowMapper) {
        try (final Connection connection = dataSource.getConnection()) {
            return query(connection, sql, statementSetter, rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query", e);
        }
    }

    public <T> List<T> query(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                             final Object... args) {
        return query(connection, sql, createStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(final Connection connection, final String sql,
                             final PreparedStatementSetter statementSetter,
                             final RowMapper<T> rowMapper) {
        log.debug("query : {}", sql);

        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            statementSetter.setValues(statement);

            try (final ResultSet resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query", e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        return singleResult(results);
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter statementSetter,
                                final RowMapper<T> rowMapper) {
        return singleResult(query(sql, statementSetter, rowMapper));
    }

    public <T> T queryForObject(final Connection connection, final String sql,
                                final RowMapper<T> rowMapper, final Object... args) {
        return singleResult(query(connection, sql, rowMapper, args));
    }

    public <T> T queryForObject(final Connection connection, final String sql,
                                final PreparedStatementSetter statementSetter,
                                final RowMapper<T> rowMapper) {
        return singleResult(query(connection, sql, statementSetter, rowMapper));
    }

    private <T> T singleResult(final List<T> results) {
        if (results.size() != 1) {
            throw new DataAccessException(
                    "Expected exactly one result, but found " + results.size()
            );
        }
        return results.get(0);
    }

    private void setParameters(final PreparedStatement statement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    private PreparedStatementSetter createStatementSetter(final Object... args) {
        return statement -> setParameters(statement, args);
    }
}
