package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeTransaction(final TransactionalAction action) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            connection.setAutoCommit(false);

            try {
                action.execute(connection);
                connection.commit();
            } catch (Exception e) {
                rollbackSafely(connection, e);
                throw new DataAccessException("트랜잭션 내부 작업 수행 중 오류가 발생하여 롤백이 수행되었습니다.", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("트랜잭션 로직 실행 중 Connection 오류가 발생했습니다.", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            releaseSafely(connection);
        }
    }

    private void rollbackSafely(Connection connection, Exception cause) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                cause.addSuppressed(rollbackEx);
            }
        }
    }

    private void releaseSafely(Connection connection) {
        if (connection != null) {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
