package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.SqlQueryException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public int executeUpdate(final Connection connection, final String query, final Object... parameters) {
        PreparedStatementCallback<Integer> preparedStatementCallback = PreparedStatement::executeUpdate;
        return execute(connection, query, preparedStatementCallback, parameters);
    }

    public <T> T executeQueryForObject(
            final Connection connection,
            final String query,
            final RowMapper<T> rowMapper,
            final Object... parameters
    ) {
        final var results = executeQueryForList(connection, query, rowMapper, parameters);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new SqlQueryException(query, "cannot map for single result");
        }
        return results.get(0);
    }

    public <T> List<T> executeQueryForList(
            final Connection connection,
            final String query,
            final RowMapper<T> rowMapper,
            final Object... parameters
    ) {
        final ResultSetExtractor<List<T>> resultSetExtractor = resultSet -> {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        };

        return executeQuery(connection, query, resultSetExtractor, parameters);
    }

    public <T> T executeQuery(
            final Connection connection,
            final String query,
            final ResultSetExtractor<T> resultSetExtractor,
            final Object... parameters
    ) {
        return execute(
                connection,
                query,
                preparedStatement -> {
                    try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                        return resultSetExtractor.extract(resultSet);
                    }
                }, parameters);
    }

    private <T> T execute(
            final Connection connection,
            final String query,
            final PreparedStatementCallback<T> callback,
            final Object... parameters
    ) {
        try (final Connection conn = connection;
             final PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            log.info("query: {}", query);
            setParameters(preparedStatement, parameters);

            return callback.doInConnection(preparedStatement);
        } catch (SQLException exception) {
            throw new SqlQueryException(exception.getMessage(), query);
        }
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object... parameters)
            throws SQLException {
        for (int index = 1; index <= parameters.length; index++) {
            preparedStatement.setString(index, String.valueOf(parameters[index - 1]));
        }
    }

}
