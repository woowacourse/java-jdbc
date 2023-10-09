package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.SqlQueryException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int executeUpdate(final String query, final Object... parameters) {
        return execute(query, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T executeQueryForObject(
            final String query,
            final RowMapper<T> rowMapper,
            final Object... parameters
    ) {
        final var results = executeQueryForList(query, rowMapper, parameters);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new SqlQueryException(query, "cannot map for single result");
        }
        return results.get(0);
    }

    public <T> List<T> executeQueryForList(
            final String query,
            final RowMapper<T> rowMapper,
            final Object... parameters
    ) {
        return executeQuery(query, resultSet -> {
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }, parameters);
    }

    public <T> T executeQuery(
            final String query,
            final ResultSetExtractor<T> resultSetExtractor,
            final Object... parameters
    ) {
        return execute(query, preparedStatement -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSetExtractor.extract(resultSet);
            }
        }, parameters);
    }

    private <T> T execute(
            final String query,
            final PreparedStatementCallback<T> callback,
            final Object... parameters
    ) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            log.info("query: {}", query);
            setParameters(preparedStatement, parameters);

            return callback.doInConnection(preparedStatement);
        } catch (SQLException exception) {
            throw new SqlQueryException(exception.getMessage(), query);
        }
        // TODO TransactionExecutor를 거치지 않을 때도 connection을 닫고 unbind 해주어야한다.
        // 하지만 논리적 트랜잭션으로 묶여있다면 여기서 해주면 안된다.
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object... parameters)
            throws SQLException {
        for (int index = 1; index <= parameters.length; index++) {
            preparedStatement.setString(index, String.valueOf(parameters[index - 1]));
        }
    }

}
