package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException;
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final PreparedStatementCallback<T> action
    ) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {

            preparedStatementSetter.execute(preparedStatement);

            return action.doInPreparedStatement(preparedStatement);

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void update(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        execute(sql,
                preparedStatementSetter, preparedStatement -> {
                    preparedStatement.executeUpdate();
                    logSql(sql);
                    return null;
                });
    }

    public <T> Optional<T> queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        final List<T> results = queryForList(sql, rowMapper, preparedStatementSetter);

        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new DataAccessException("Incorrect result size: expected 1, but got " + results.size());
        }
        return Optional.of(results.getFirst());
    }

    public <T> List<T> queryForList(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        return execute(sql,
                preparedStatementSetter, preparedStatement -> {
                    try (final var queryResultSet = preparedStatement.executeQuery()) {

                        logSql(sql);

                        final var objectMappingResultSet = new ArrayList<T>();

                        while (queryResultSet.next()) {
                            objectMappingResultSet.add(rowMapper.mapRow(queryResultSet));
                        }
                        return objectMappingResultSet;
                    }
                });
    }

    private void logSql(String sql) {
        log.info("query: {}", sql);
    }
}
