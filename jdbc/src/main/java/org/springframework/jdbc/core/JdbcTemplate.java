package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.SqlQueryException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final ConnectionManager connectionManager;

    // TODO Connection은 모두 외부에서 받게 한 뒤 ConnectionManager 제거
    public JdbcTemplate(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public int executeUpdate(final String query, final Object... parameters) {
        try (final Connection connection = connectionManager.getConnection()) {
            return executeUpdate(connection, query, parameters);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public int executeUpdate(final Connection connection, final String query, final Object... parameters) {
        PreparedStatementCallback<Integer> preparedStatementCallback = PreparedStatement::executeUpdate;
        return execute(connection, query, preparedStatementCallback, parameters);
    }

    public <T> T executeQueryForObject(
            final String query,
            final RowMapper<T> rowMapper,
            final Object... parameters
    ) {
        try (final Connection connection = connectionManager.getConnection()) {
            return executeQueryForObject(connection, query, rowMapper, parameters);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
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


    // TODO 불필요한 오버로딩 삭제
    public <T> List<T> executeQueryForList(
            final String query,
            final RowMapper<T> rowMapper,
            final Object... parameters
    ) {
        try (final Connection connection = connectionManager.getConnection()) {
            return executeQueryForList(connection, query, rowMapper, parameters);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
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
        try (final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
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
