package com.interface21.jdbc.core;

import java.sql.Connection;

import javax.sql.DataSource;

public class GeneralTransactionManager implements TransactionManager {

    private final DataSource dataSource;
    private final ThreadLocal<Connection> connection = new ThreadLocal<>();
    private final ThreadLocal<Integer> transactionCount = new ThreadLocal<>();
    private final ThreadLocal<Boolean> rollbackOnly = new ThreadLocal<>();

    public GeneralTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() {
        try {
            // propagation level: REQUIRED
            if (connection.get() == null) {
                Connection connection1 = dataSource.getConnection();
                connection1.setAutoCommit(false);
                connection.set(connection1);
                transactionCount.set(1);
                rollbackOnly.set(false);
            } else {
                // 중첩 트랜잭션인 경우 카운트 증가
                transactionCount.set(transactionCount.get() + 1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to begin transaction", e);
        }
    }

    public Connection getCurrentConnection() {
        Connection conn = connection.get();
        if (conn == null) {
            throw new IllegalStateException("No transaction started for this thread.");
        }
        return conn;
    }

    public void commit() {
        Connection conn = connection.get();
        if (conn == null) {
            throw new IllegalStateException("No transaction started for this thread.");
        }

        int count = transactionCount.get();
        transactionCount.set(count - 1);

        if (count == 1) {
            // 최상위 트랜잭션에서만 실제 commit
            try {
                if (rollbackOnly.get()) {
                    conn.rollback();
                } else {
                    conn.commit();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to commit transaction", e);
            } finally {
                cleanupConnection(conn);
            }
        }
    }

    public void rollback() {
        Connection conn = connection.get();
        if (conn == null) {
            throw new IllegalStateException("No transaction started for this thread.");
        }

        int count = transactionCount.get();
        transactionCount.set(count - 1);
        if (count == 1) {
            // 최상위 트랜잭션에서만 실제 rollback
            try {
                conn.rollback();
            } catch (Exception e) {
                throw new RuntimeException("Failed to rollback transaction", e);
            } finally {
                cleanupConnection(conn);
            }
        } else {
            // 중첩 트랜잭션인 경우 롤백 표시
            rollbackOnly.set(true);
        }
    }

    private void cleanupConnection(Connection conn) {
        try {
            try {
                conn.setAutoCommit(true);
            } catch (Exception ignored) {
            }
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        } finally {
            connection.remove();
            transactionCount.remove();
        }
    }
}
