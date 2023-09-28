package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.SqlQueryException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final ConnectionManager connectionManager;

    public JdbcTemplate(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void executeUpdate(String query, Object... parameters) {
        try (final Connection connection = connectionManager.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            log.info("query: {}", query);
            setParameters(preparedStatement, parameters);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new SqlQueryException(exception.getMessage(), query);
        }
    }

    public <T> T executeQuery(String query, RowMapper<T> rowMapper, Object... parameters) {
        try (final Connection connection = connectionManager.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = executePreparedStatementQuery(preparedStatement, parameters)) {
            log.info("query: {}", query);
            return rowMapper.mapRow(resultSet);
        } catch (SQLException exception) {
            throw new SqlQueryException(exception.getMessage(), query);
        }
    }

    private ResultSet executePreparedStatementQuery(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        setParameters(preparedStatement, parameters);
        return preparedStatement.executeQuery();
    }

    private void setParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        for (int index = 1; index <= parameters.length; index++) {
            preparedStatement.setString(index, String.valueOf(parameters[index - 1]));
        }
    }

}
