package com.interface21.transaction.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    public static void executeWithTransaction(final Runnable method, final DataSource dataSource) {
        Connection connection = connect(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try (connection) {
            connection.setAutoCommit(false);
            method.run();
            connection.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            rollback(connection);
            throw new DataAccessException("Failed to commit transaction.", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private static Connection connect(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to connect.", e);
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to rollback transaction.", e);
        }
    }
}
