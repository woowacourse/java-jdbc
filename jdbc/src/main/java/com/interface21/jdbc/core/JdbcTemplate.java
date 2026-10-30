package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
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

    public int update(final String sql, final Object... parameters) {
        return execute(sql, parameters, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, parameters, statement -> {
            try (final var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    /**
     * 조회 결과가 없으면 null을 반환하고, 두 건 이상이면 IncorrectResultSizeDataAccessException을 던진다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, parameters, statement -> {
            try (final var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                final var result = rowMapper.mapRow(resultSet);
                if (resultSet.next()) {
                    throw new IncorrectResultSizeDataAccessException("Expected at most one row but found at least 2: " + sql);
                }
                return result;
            }
        });
    }

    private void bindParameters(final PreparedStatement statement, final Object[] parameters) throws SQLException {
        for (int index = 0; index < parameters.length; index++) {
            statement.setObject(index + 1, parameters[index]);
        }
    }

    private <T> T execute(final String sql, final Object[] parameters, final StatementCallback<T> callback) {
        log.debug("query : {}", sql);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            bindParameters(statement, parameters);
            return callback.execute(statement);
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
