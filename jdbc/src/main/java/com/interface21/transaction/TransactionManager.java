package com.interface21.transaction;

import com.interface21.transaction.exception.TransactionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() {
        try {
            final Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, conn);
        } catch (SQLException e) {
            throw new TransactionException("트랜잭션 시작 실패");
        }
    }

    public void commit() {
        final Connection conn = findConnection();
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new TransactionException("트랜잭션 커밋 실패", e);
        } finally {
            closeConnection(conn);
        }
    }

    public void rollback() {
        final Connection conn = findConnection();
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new TransactionException("트랜잭션 롤백 실패", e);
        } finally {
            log.warn("rollback");
            closeConnection(conn);
        }
    }

    private Connection findConnection() {
        final Connection conn = TransactionSynchronizationManager.getResource(dataSource);
        if (conn == null) {
            throw new TransactionException("활성화된 트랜잭션이 존재하지 않습니다.");
        }
        return conn;
    }

    private void closeConnection(final Connection connection) {
        try {
            connection.setAutoCommit(true);
            connection.close();
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (SQLException e) {
            throw new TransactionException("커넥션 닫기 실패", e);
        }
    }
}
