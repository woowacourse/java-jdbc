package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.TransactionConnection;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int SINGLE_RESULT_SIZE = 1;

    private final DataSource dataSource;
    private final PreparedStatementSetter statementSetter;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        this.statementSetter = new PreparedStatementSetter();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        TransactionConnection transactionConnection = connect();
        Connection connection = transactionConnection.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            statementSetter.setValues(preparedStatement, arguments);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("An error occurred during the execution of the select query.", e);
        } finally {
            transactionConnection.closeIfNotInTransaction();
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        List<T> results = query(sql, rowMapper, arguments);

        if (results.size() > SINGLE_RESULT_SIZE) {
            throw new JdbcException("multiple rows found.");
        }
        if (results.size() == SINGLE_RESULT_SIZE) {
            return results.getFirst();
        }
        return null;
    }

    public void update(final String sql, final Object... arguments) {
        TransactionConnection transactionConnection = connect();
        Connection connection = transactionConnection.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            statementSetter.setValues(preparedStatement, arguments);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("An error occurred during the execution of the update query.", e);
        } finally {
            transactionConnection.closeIfNotInTransaction();
        }
    }

    private TransactionConnection connect() {
        try {
            Connection connection = TransactionSynchronizationManager.getResource(dataSource);
            if (connection == null) {
                return new TransactionConnection(dataSource.getConnection(), false);
            }
            return new TransactionConnection(connection, true);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to connect.", e);
        }
    }
}
