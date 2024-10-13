package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

import javax.sql.DataSource;

import com.interface21.jdbc.exception.ConnectionCloseException;
import com.interface21.jdbc.exception.TransactionExecutionException;
import com.interface21.jdbc.exception.TransactionRollbackException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Consumer<Connection> consumer) {
        Connection connection = null;
        boolean originalAutoCommit = true;

        try {
            connection = dataSource.getConnection();
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (final Exception e) {
            rollback(connection);
            throw new TransactionExecutionException("트랜잭션 실행에 실패하였습니다.", e);
        } finally {
            closeConnection(connection, originalAutoCommit);
        }
    }

    private void rollback(final Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (final SQLException e) {
            throw new TransactionRollbackException("롤백에 실패하였습니다.", e);
        }
    }

    private void closeConnection(final Connection connection, final boolean originalAutoCommit) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.setAutoCommit(originalAutoCommit);
                connection.close();
            }
        } catch (final SQLException e) {
            throw new ConnectionCloseException("커넥션 종료에 실패하였습니다.", e);
        }
    }
}
