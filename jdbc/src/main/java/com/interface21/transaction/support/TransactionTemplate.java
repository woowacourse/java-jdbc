package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable action) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            try {
                action.run();
                connection.commit();
            } catch (RuntimeException | SQLException e) {
                rollback(connection, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("트랜잭션 처리에 실패했습니다", e);
        } finally {
            end(connection);
        }
    }

    private void rollback(final Connection connection, final Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            cause.addSuppressed(rollbackFailure);
        }
    }

    private void end(final Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            TransactionSynchronizationManager.unbindResource(dataSource);
            closeQuietly(connection);
            return;
        }
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    private void closeQuietly(final Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
