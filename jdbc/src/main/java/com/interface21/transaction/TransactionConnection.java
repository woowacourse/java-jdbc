package com.interface21.transaction;

import com.interface21.jdbc.core.JdbcException;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionConnection {

    private final Connection connection;
    private final boolean isInTransaction;

    public TransactionConnection(final Connection connection, final boolean isInTransaction) {
        this.connection = connection;
        this.isInTransaction = isInTransaction;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeIfNotInTransaction() {
        if (isInTransaction) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new JdbcException("Failed to close connection.", e);
        }
    }
}
