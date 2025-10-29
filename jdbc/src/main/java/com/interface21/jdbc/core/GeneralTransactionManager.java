package com.interface21.jdbc.core;

import java.sql.Connection;

import javax.sql.DataSource;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

public class GeneralTransactionManager implements TransactionManager {

    public void begin(DataSource dataSource) {
        try {
            Connection connection1 = DataSourceUtils.getConnection(dataSource);
            TransactionSynchronizationManager.setRollbackOnly(false);
            // 중첩 트랜잭션인 경우 카운트 증가
            Integer currentTransactionCount = TransactionSynchronizationManager.getTransactionCount();
            if (currentTransactionCount == null) {
                connection1.setAutoCommit(false);
                TransactionSynchronizationManager.setTransactionCount(1);
            } else {
                TransactionSynchronizationManager.setTransactionCount(currentTransactionCount + 1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to begin transaction", e);
        }
    }

    public Connection getCurrentConnection(DataSource dataSource) {
        return DataSourceUtils.getConnection(dataSource);
    }

    public void commit(DataSource dataSource) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        int count = TransactionSynchronizationManager.getTransactionCount();
        TransactionSynchronizationManager.setTransactionCount(count - 1);

        if (count == 1) {
            // 최상위 트랜잭션에서만 실제 commit
            try {
                if (TransactionSynchronizationManager.isRollbackOnly()) {
                    conn.rollback();
                } else {
                    conn.commit();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to commit transaction", e);
            } finally {
                cleanupConnection(dataSource, conn);
            }
        }
    }

    public void rollback(DataSource dataSource) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        int count = TransactionSynchronizationManager.getTransactionCount();
        TransactionSynchronizationManager.setTransactionCount(count - 1);
        if (count == 1) {
            // 최상위 트랜잭션에서만 실제 rollback
            try {
                conn.rollback();
            } catch (Exception e) {
                throw new RuntimeException("Failed to rollback transaction", e);
            } finally {
                cleanupConnection(dataSource, conn);
            }
        } else {
            // 중첩 트랜잭션인 경우 롤백 표시
            TransactionSynchronizationManager.setRollbackOnly(true);
        }
    }

    private void cleanupConnection(DataSource dataSource, Connection conn) {
        try {
            try {
                conn.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.removeTransactionCount();
            TransactionSynchronizationManager.removeRollbackOnly();
        }
    }
}
