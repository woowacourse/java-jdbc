package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    TxUserService(final UserService userService, final DataSource dataSource) {
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
    public void changePassword(final long id, final String newPassword, final String createBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("Connection already bound to DataSource");
        }
        final Connection connection;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
        } catch (final CannotGetJdbcConnectionException e) {
            throw new DataAccessException("Failed to change password, userId=" + id, e.getCause());
        }

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        boolean committed = false;
        Throwable failure = null;
        try {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                userService.changePassword(id, newPassword, createBy);
                connection.commit();
                committed = true;
            } catch (final SQLException | RuntimeException | Error e) {
                rollbackAndRestoreAutoCommit(connection, autoCommit, e);
                throw e;
            }
            connection.setAutoCommit(autoCommit);
        } catch (final SQLException e) {
            failure = e;
            throw transactionFailure(id, committed, e);
        } catch (final RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (final CannotGetJdbcConnectionException e) {
                if (failure == null) {
                    throw transactionFailure(id, committed, e.getCause());
                }
                failure.addSuppressed(e.getCause());
            } catch (final RuntimeException | Error e) {
                if (failure == null) {
                    throw e;
                }
                failure.addSuppressed(e);
            }
        }
    }

    private DataAccessException transactionFailure(final long id, final boolean committed, final Throwable cause) {
        if (committed) {
            return new TransactionCleanupException("Password change committed, but connection cleanup failed, userId=" + id, cause);
        }
        return new DataAccessException("Failed to change password, userId=" + id, cause);
    }

    private void rollbackAndRestoreAutoCommit(final Connection connection, final boolean autoCommit,
                                             final Throwable failure) {
        try {
            connection.rollback();
            connection.setAutoCommit(autoCommit);
        } catch (final SQLException e) {
            failure.addSuppressed(e);
        }
    }
}
