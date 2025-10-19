package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.Getter;

@Getter
public class Transaction {

    private final DataSource dataSource;

    private Transaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static Transaction init(DataSource dataSource) {
        return new Transaction(dataSource);
    }

    public void begin() {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            TransactionSynchronizationManager.bindConnection(dataSource, conn);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to begin transaction", e);
        }
    }

    public void commit() {
        final Connection connection = TransactionSynchronizationManager.getConnection(dataSource);
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit transaction", e);
        } finally {
            releaseConnection(connection);
        }
    }

    public void rollback() {
        final Connection connection = TransactionSynchronizationManager.getConnection(dataSource);
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to rollback transaction", e);
        } finally {
            releaseConnection(connection);
        }
    }

    private void releaseConnection(final Connection connection) {
        TransactionSynchronizationManager.unbindConnection(dataSource);
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to close connection", e);
        }
    }
}
