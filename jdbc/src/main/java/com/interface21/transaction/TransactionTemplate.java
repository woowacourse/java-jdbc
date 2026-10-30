package com.interface21.transaction;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeWithoutResult(final Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }

    /**
     * 연결 하나를 열어 현재 스레드에 바인딩한 뒤 콜백을 실행하고 commit한다.
     * 콜백 안에서 JdbcTemplate을 거치는 모든 접근은 DataSourceUtils를 통해 이 연결을 쓴다.
     * 이미 진행 중인 트랜잭션이 있으면 거기에 참여하고, commit과 rollback은 바깥 트랜잭션에 맡긴다.
     */
    public <T> T execute(final TransactionCallback<T> callback) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            return callback.doInTransaction();
        }
        final var connection = begin();
        var restoreAutoCommit = true;
        try {
            final var result = callback.doInTransaction();
            commit(connection);
            return result;
        } catch (RuntimeException | Error e) {
            restoreAutoCommit = rollback(connection, e);
            throw e;
        } finally {
            end(connection, restoreAutoCommit);
        }
    }

    private Connection begin() {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            DataSourceUtils.releaseConnection(connection, dataSource);
            throw new TransactionException("Failed to begin transaction", e);
        }
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        return connection;
    }

    private static void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new TransactionException("Failed to commit transaction", e);
        }
    }

    /**
     * rollback 성공 여부를 돌려준다. 실패한 연결은 autoCommit을 되돌리면 안 된다.
     * JDBC 규약상 autoCommit 모드를 바꾸면 진행 중인 트랜잭션이 commit되기 때문이다.
     */
    private static boolean rollback(final Connection connection, final Throwable cause) {
        try {
            connection.rollback();
            return true;
        } catch (SQLException e) {
            cause.addSuppressed(e);
            return false;
        }
    }

    private void end(final Connection connection, final boolean restoreAutoCommit) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        if (restoreAutoCommit) {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                log.warn("Failed to restore autoCommit", e);
            }
        }
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
