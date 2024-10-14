package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

import javax.sql.DataSource;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.ConnectionCloseException;
import com.interface21.jdbc.exception.TransactionExecutionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Consumer<Connection> consumer) {
        Connection connection = null;

        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (final Exception e) {
            rollback(connection);
            throw new TransactionExecutionException("트랜잭션 실행에 실패하였습니다.", e);
        } finally {
            closeConnection(connection);
        }
    }

    private void rollback(final Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (final SQLException e) {
            throw new TransactionExecutionException("롤백에 실패하였습니다.", e);
        }
    }

    private void closeConnection(final Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                DataSourceUtils.releaseConnection(connection, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (final SQLException e) {
            throw new ConnectionCloseException("커넥션 종료에 실패했습니다.", e);
        }
    }
}
