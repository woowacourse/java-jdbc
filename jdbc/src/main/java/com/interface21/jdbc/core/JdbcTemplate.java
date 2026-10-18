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
        log.debug("query : {}", sql);

        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setParameters(statement, parameters);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return executeQuery(sql, resultSet -> {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }, parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return executeQuery(sql, resultSet -> {
            if (!resultSet.next()) {
                return null;
            }
            final T result = rowMapper.mapRow(resultSet);
            if (resultSet.next()) {
                throw new DataAccessException("Expected at most one row, but found at least 2");
            }
            return result;
        }, parameters);
    }

    private <T> T executeQuery(final String sql, final ResultSetExtractor<T> extractor, final Object[] parameters) {
        log.debug("query : {}", sql);

        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setParameters(statement, parameters);

            try (final var resultSet = statement.executeQuery()) {
                return extractor.extract(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    @FunctionalInterface
    private interface ResultSetExtractor<T> {

        T extract(ResultSet resultSet) throws SQLException;
    }
}
