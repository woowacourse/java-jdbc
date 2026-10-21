package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
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

    public int update(final String sql, final Object... parameters) {
        return update(sql, parameterSetter(parameters));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, parameterSetter(parameters), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return executeQuery(sql, setter, resultSet -> {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return queryForObject(sql, parameterSetter(parameters), rowMapper);
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return executeQuery(sql, setter, resultSet -> {
            if (!resultSet.next()) {
                return null;
            }
            final T result = rowMapper.mapRow(resultSet);
            if (resultSet.next()) {
                throw new DataAccessException("Expected at most one row, but found at least 2");
            }
            return result;
        });
    }

    private <T> T executeQuery(final String sql, final PreparedStatementSetter setter,
                             final ResultSetExtractor<T> extractor) {
        return execute(sql, setter, statement -> {
            try (final var resultSet = statement.executeQuery()) {
                return extractor.extract(resultSet);
            }
        });
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                         final PreparedStatementCallback<T> callback) {
        log.debug("query : {}", sql);

        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private PreparedStatementSetter parameterSetter(final Object[] parameters) {
        return statement -> {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
        };
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    private interface ResultSetExtractor<T> {

        T extract(ResultSet resultSet) throws SQLException;
    }
}
