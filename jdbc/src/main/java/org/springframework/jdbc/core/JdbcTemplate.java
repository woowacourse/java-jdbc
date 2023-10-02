package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.core.TransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final TransactionManager connectionManager;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        this.connectionManager = new TransactionManager(dataSource);
    }

    public int update(String sql, Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, statement -> fetchData(statement, new RowMapResultSetExecutor<>(rowMapper)), args);
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException();
        }
        return Optional.ofNullable(results.get(0));
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> executor, Object... args) {
        Connection connection = connectionManager.getConnection();
        try (
                PreparedStatement statement = getPreparedStatement(sql, connection, args);
        ) {
            log.debug("query : {}", sql);
            return executor.fetchData(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Connection connection, Object... args) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        return preparedStatement;
    }

    private <T> T fetchData(PreparedStatement statement, ResultSetExecutor<T> executor) {
        try (ResultSet resultSet = statement.executeQuery()) {
            return executor.extractData(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
