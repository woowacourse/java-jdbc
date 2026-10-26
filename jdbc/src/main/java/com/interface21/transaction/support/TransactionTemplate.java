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

    public void execute(final Runnable action) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean originalAutoCommit = true;
        boolean rollbackFailed = false; // 롤백 실패 여부를 기록할 플래그

        try {
            originalAutoCommit = connection.getAutoCommit();
            if (originalAutoCommit) {
                connection.setAutoCommit(false);
            }

            try {
                action.run();
                connection.commit();
            } catch (RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    rollbackFailed = true;
                    e.addSuppressed(rollbackFailure);
                }
                throw e;
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    rollbackFailed = true;
                    e.addSuppressed(rollbackFailure);
                }
                throw new DataAccessException("트랜잭션 처리에 실패했습니다", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("트랜잭션 처리에 실패했습니다", e);
        } finally {
            end(connection, originalAutoCommit, rollbackFailed);
        }
    }

    private void end(final Connection connection, final boolean originalAutoCommit, final boolean rollbackFailed) {
        // 롤백이 실패한 커넥션은 상태를 원복하지 않고 즉시 폐기하여 커넥션 풀 오염 방지
        if (rollbackFailed) {
            TransactionSynchronizationManager.unbindResource(dataSource);
            closeQuietly(connection);
            return;
        }

        try {
            if (connection != null && !connection.isClosed()) {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            TransactionSynchronizationManager.unbindResource(dataSource);
            closeQuietly(connection);
            return;
        }

        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    private void closeQuietly(final Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
