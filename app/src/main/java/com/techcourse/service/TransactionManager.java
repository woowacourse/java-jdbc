package com.techcourse.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionCallback<T> callback) {
        try {
            final Connection connection = TransactionSynchronizationManager.startNewTransaction(dataSource);

            final T result = callback.execute();

            commitTransaction(connection);
            TransactionSynchronizationManager.finishTransaction(dataSource);

            return result;
        } catch (final SQLException ex) {
            throw new RuntimeException("실행 중 예외가 발생했습니다.");
        }
    }

    public static void commitTransaction(final Connection connection) {
        try {
            connection.commit();

            clear(connection);
        } catch (final SQLException ex) {
            rollback(connection);

            throw new DataAccessException("실행 중 예외가 발생했습니다.");
        }
    }

    public static void rollback(final Connection connection) {
        try {
            connection.rollback();

            clear(connection);
        } catch (final SQLException ex) {
            throw new DataAccessException("트랜잭션 롤백 중 예외가 발생했습니다.");
        }
    }

    private static void clear(final Connection connection) {
        try {
            connection.setAutoCommit(true);
            connection.close();
            DataSourceUtils.releaseConnection(connection);
        } catch (final SQLException ex) {
            throw new DataAccessException("커넥션 종료 중 예외가 발생했습니다.");
        }
    }
}
