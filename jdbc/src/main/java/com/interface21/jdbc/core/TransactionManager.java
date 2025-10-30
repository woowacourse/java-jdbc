package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void start() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("이미 트랜잭션이 시작되었습니다.");
        }
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
        } catch (SQLException e) {
            log.error("트랜잭션 시작 중 오류 발생", e);
            throw new DataAccessException("트랜잭션 시작 실패", e);
        }
    }

    public void commit() {
        try {
            Connection connection = getCurrentConnection();
            if (connection == null) {
                throw new IllegalStateException("트랜잭션이 시작되지 않았습니다.");
            }
            connection.commit();
        } catch (SQLException e) {
            log.error("트랜잭션 커밋 중 오류 발생", e);
            throw new DataAccessException("트랜잭션 커밋 실패", e);
        } finally {
            cleanup();
        }
    }

    public void rollback() {
        try {
            Connection connection = getCurrentConnection();
            if (connection == null) {
                throw new IllegalStateException("트랜잭션이 시작되지 않았습니다.");
            }
            connection.rollback();
        } catch (SQLException e) {
            log.error("트랜잭션 롤백 중 오류 발생", e);
            throw new DataAccessException("트랜잭션 롤백 실패", e);
        } finally {
            cleanup();
        }
    }

    private Connection getCurrentConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    private void cleanup() {
        Connection connection = getCurrentConnection();
        if (connection != null) {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (Exception e) {
                log.error("Connection 정리 중 오류 발생", e);
            }
        }
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
}
