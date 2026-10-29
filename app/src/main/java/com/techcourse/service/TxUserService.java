package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            userService.changePassword(id, newPassword, createBy);
            return;
        }

        final Connection connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            executeInTransaction(connection, () -> userService.changePassword(id, newPassword, createBy));
        } catch (SQLException e) {
            final var exception = new DataAccessException("트랜잭션 처리에 실패했습니다.", e);
            failure = exception;
            throw exception;
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            releaseConnection(connection, failure);
        }
    }

    private void executeInTransaction(final Connection connection, final Runnable action) throws SQLException {
        final boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable failure = null;
        boolean completed = false;
        try {
            action.run();
            connection.commit();
            completed = true;
        } catch (RuntimeException | Error | SQLException e) {
            failure = e;
            completed = rollback(connection, e);
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            if (completed) {
                restoreAutoCommit(connection, autoCommit, failure);
            }
        }
    }

    private boolean rollback(final Connection connection, final Throwable cause) {
        try {
            connection.rollback();
            return true;
        } catch (SQLException e) {
            cause.addSuppressed(e);
            return false;
        }
    }

    private void restoreAutoCommit(final Connection connection, final boolean autoCommit, final Throwable cause)
            throws SQLException {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            if (cause == null) {
                throw e;
            }
            cause.addSuppressed(e);
        }
    }

    private void releaseConnection(final Connection connection, final Throwable cause) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (RuntimeException e) {
            if (cause == null) {
                throw e;
            }
            cause.addSuppressed(e);
        }
    }
}
