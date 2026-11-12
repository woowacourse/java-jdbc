package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable callback) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("이미 진행 중인 트랜잭션이 있습니다.");
        }
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                executeTransaction(connection, callback);
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (RuntimeException | Error e) {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    private void executeTransaction(final Connection connection, final Runnable callback) {
        try {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                callback.run();
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                rollbackAndRestoreAutoCommit(connection, autoCommit, e);
                throw e;
            }

            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DataAccessException("트랜잭션 처리에 실패했습니다.", e);
        }
    }

    private void rollbackAndRestoreAutoCommit(
            final Connection connection,
            final boolean autoCommit,
            final Throwable failure
    ) {
        try {
            connection.rollback();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            failure.addSuppressed(e);
        }
    }
}
