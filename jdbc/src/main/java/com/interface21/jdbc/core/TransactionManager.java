package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void start() {
        if (connectionHolder.get() != null) {
            throw new IllegalStateException("이미 트랜잭션이 시작되었습니다.");
        }
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            connectionHolder.set(connection);
        } catch (SQLException e) {
            log.error("트랜잭션 시작 중 오류 발생", e);
            throw new DataAccessException("트랜잭션 시작 실패", e);
        }
    }

    public Connection getCurrentConnection() {
        return connectionHolder.get();
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
            end();
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
            end();
        }
    }

    private void end() {
        Connection connection = connectionHolder.get();
        connectionHolder.remove();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Connection 정리 중 오류 발생", e);
            }
        }
    }
}
