package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.CannotCloseJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            tryRollback(connection);
        }
    }

    private void tryRollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new TransactionSystemException("롤백에 실패했습니다.", ex);
        }
    }

    public void commit() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.commit();
        } catch (SQLException e) {
            tryRollback(connection);
        } finally {
            tryCloseConnection();
        }
    }

    private void tryCloseConnection() {
        try {
            final Connection connection = TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        } catch (SQLException e) {
            throw new CannotCloseJdbcConnectionException("JDBC Connection 을 닫는데 실패했습니다.", e);
        }
    }

    public void rollback() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new TransactionSystemException("롤백에 실패했습니다.", e);
        } finally {
            tryCloseConnection();
        }
    }
}
