package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.JdbcAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... values) {
        return update(sql, new TypedPreparedStatementSetter(values));
    }

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setParameters(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... values) {
        return queryForList(sql, rowMapper, new TypedPreparedStatementSetter(values));
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setParameters(preparedStatement);
            return MappedResultSet.create(rowMapper, preparedStatement)
                    .getResults();
        });
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        return queryForObject(sql, rowMapper, new TypedPreparedStatementSetter(values));
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setParameters(preparedStatement);
            return MappedResultSet.create(rowMapper, preparedStatement, 1)
                    .getFirst();
        });
    }

    public <T> T execute(String sql, SqlFunction<PreparedStatement, T> action) {
        boolean isNewConnection = TransactionSynchronizationManager.doesNotManage(dataSource);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("Executing query: {}", sql);

            return action.apply(preparedStatement);
        } catch (SQLException e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            throw new JdbcAccessException("Error executing query: " + sql, e);
        } finally {
            closeIfNecessary(isNewConnection, connection);
        }
    }

    private void closeIfNecessary(boolean isNewConnection, Connection connection) {
        if (isNewConnection) {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
