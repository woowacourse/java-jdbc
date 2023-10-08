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

    public void doInTransaction(final Runnable runnable) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (SQLException e) {
            tryRollback(connection);
        } finally {
            tryCloseConnection();
        }
    }

    private void tryRollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new TransactionRollbackException("롤백에 실패했습니다.", ex);
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
}
