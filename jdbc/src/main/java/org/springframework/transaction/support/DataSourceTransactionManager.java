package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DataSourceTransactionManager {

    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    private final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void startTransaction() {
        try {
            var connection = getConnection();
            connection.setAutoCommit(false);
            connectionHolder.set(connection);
        } catch (SQLException e) {
            throw new RuntimeException("트랜잭션 시작 실패", e);
        }
    }

    public Connection getConnection() {
        var connection = connectionHolder.get();
        if (connection != null) {
            return connection;
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("커넥션 획득 실패", e);
        }
    }

    public void commit() {
        try {
            var connection = getConnection();
            connection.commit();
            close(connection, true);
        } catch (SQLException e) {
            throw new RuntimeException("커밋 실패", e);
        }
    }

    public void rollback() {
        try {
            var connection = getConnection();
            connection.rollback();
            close(connection, true);
        } catch (SQLException e) {
            throw new RuntimeException("롤백 실패", e);
        }
    }

    public void release(Connection connection) {
        if (connectionHolder.get() != connection) {
            close(connection, false);
        }
    }

    private void close(Connection connection, boolean clear) {
        if (clear) {
            connectionHolder.remove();
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("커넥션 종료 실패", e);
        }
    }
}
