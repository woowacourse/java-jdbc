package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    private final ConnectionManager connectionManager;

    public TransactionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public <T> T transaction(Function<Connection, T> function) {
        Connection connection = connectionManager.getConnection();
        try (connection) {
            beginTransaction(connection);
            T result = function.apply(connection);
            endTransaction(connection);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    public void transaction(Consumer<Connection> consumer) {
        Connection connection = connectionManager.getConnection();
        try (connection) {
            beginTransaction(connection);
            consumer.accept(connection);
            endTransaction(connection);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private void beginTransaction(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
    }

    private void endTransaction(Connection connection) throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException sqlException) {
            throw new DataAccessException(sqlException);
        }
    }
}
