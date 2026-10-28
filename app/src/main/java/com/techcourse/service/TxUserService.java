package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("Nested transactions are not supported");
        }
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable failure = null;
        try {
            changePassword(connection, id, newPassword, createdBy);
        } catch (SQLException e) {
            final var exception = new DataAccessException(e);
            failure = exception;
            throw exception;
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            releaseConnection(connection, failure);
        }
    }

    private void changePassword(
            final Connection connection,
            final long id,
            final String newPassword,
            final String createdBy
    ) throws SQLException {
        final var originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        boolean transactionCompleted = false;
        Throwable failure = null;
        try {
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
            transactionCompleted = true;
        } catch (SQLException | RuntimeException | Error e) {
            failure = e;
            transactionCompleted = rollback(connection, e);
            throw e;
        } finally {
            if (transactionCompleted) {
                restoreAutoCommit(connection, originalAutoCommit, failure);
            }
        }
    }

    private void releaseConnection(final Connection connection, final Throwable cause) {
        try {
            TransactionSynchronizationManager.unbindResource(dataSource);
        } finally {
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

    private boolean rollback(
            final Connection connection,
            final Throwable cause
    ) {
        try {
            connection.rollback();
            return true;
        } catch (SQLException e) {
            cause.addSuppressed(e);
            return false;
        }
    }

    private void restoreAutoCommit(
            final Connection connection,
            final boolean originalAutoCommit,
            final Throwable cause
    ) throws SQLException {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            if (cause == null) {
                throw e;
            }
            cause.addSuppressed(e);
        }
    }
}
