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

    public void execute(final Runnable runnable) {
        final boolean hadExistingConnection = DataSourceUtils.hasResource(dataSource);
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            final boolean connectionWasInAutoCommitMode = isAutoCommitEnabled(connection);

            // 이미 트랜잭션이 시작된 경우
            if (hadExistingConnection && !connectionWasInAutoCommitMode) {
                runnable.run();
                return;
            }

            if (connectionWasInAutoCommitMode) {
                connection.setAutoCommit(false);
            }

            runnable.run();
            // 우리가 만든 트랜잭션인 경우
            if (!hadExistingConnection) {
                connection.commit();
            }
        } catch (final RuntimeException | SQLException e) {
            if (!hadExistingConnection) {
                rollback(connection, e);
            } else {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new DataAccessException(e);
                }
            }
        } finally {
            // 원래 있던 연결이 아니라면
            if (!hadExistingConnection) {
                DataSourceUtils.releaseConnection(connection, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }
    }

    private boolean isAutoCommitEnabled(final Connection connection) {
        try {
            return connection.getAutoCommit();
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to check auto-commit status", e);
        }
    }

    private void rollback(final Connection connection, final Exception originalException) {
        try {
            connection.rollback();
        } catch (final SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
        throw new DataAccessException("트랜잭션 실행 실패", originalException);
    }
}
