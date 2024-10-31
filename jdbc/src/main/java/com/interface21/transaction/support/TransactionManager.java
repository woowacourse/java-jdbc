package com.interface21.transaction.support;

import com.interface21.jdbc.core.JdbcTemplateException;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final Connection connection;

    public TransactionManager(Connection connection) {
        this.connection = connection;
    }

    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new JdbcTemplateException("트랜잭션을 시작할 수 없습니다.", e);
        }
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            rollback();
            throw new JdbcTemplateException("트랜잭션 커밋 중 예외가 발생했습니다. 롤백합니다.", e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new JdbcTemplateException("트랜잭션 롤백 중 예외가 발생했습니다.", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.setAutoCommit(true);
                connection.close();
            }
        } catch (SQLException e) {
            throw new JdbcTemplateException("Connection을 닫는 중 오류가 발생했습니다.", e);
        }
    }
}
