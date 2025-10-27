package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeInTransaction(final Consumer<Connection> callback) {
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                callback.accept(connection);
                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }

                if (e instanceof DataAccessException) {
                    throw (DataAccessException) e;
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new DataAccessException("Transaction failed", e);
            }
        } catch (SQLException connectionEx) {
            throw new DataAccessException("Failed to set connection", connectionEx);
        }
    }
}
