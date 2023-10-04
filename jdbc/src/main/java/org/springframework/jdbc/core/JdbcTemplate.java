package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.SqlQueryException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final ConnectionManager connectionManager;

    public JdbcTemplate(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public int executeUpdate(final String query, final Object... parameters) {
        return execute(query, (connection, preparedStatement) -> preparedStatement.executeUpdate(), parameters);
    }

    public <T> T executeQueryForObject(
            final String query,
            final RowMapper<T> rowMapper,
            final Object... parameters
    ) {
        final ResultSetExtractor<T> resultSetExtractor = resultSet -> {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        };

        return executeQuery(query, resultSetExtractor, parameters);
    }

    public <T> List<T> executeQueryForList(
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

        return executeQuery(query, resultSetExtractor, parameters);
    }

    public <T> T executeQuery(
            final String query,
            final ResultSetExtractor<T> resultSetExtractor,
            final Object... parameters
    ) {
        return execute(query, (connection, preparedStatement) -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSetExtractor.extract(resultSet);
            }
        }, parameters);
    }

    private <T> T execute(
            final String query,
            final ConnectionCallback<T> callback,
            final Object... parameters
    ) {
        try (final Connection connection = connectionManager.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            log.info("query: {}", query);
            setParameters(preparedStatement, parameters);
            return callback.doInConnection(connection, preparedStatement);
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
