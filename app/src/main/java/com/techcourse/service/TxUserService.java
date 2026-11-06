package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this(userService, DataSourceConfig.getInstance());
    }

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
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable failure = null;
        try {
            changePasswordInTransaction(connection, id, newPassword, createBy);
        } catch (SQLException e) {
            final var exception = new DataAccessException(e);
            failure = exception;
            throw exception;
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            releaseConnection(connection, failure);
        }
    }

    private void changePasswordInTransaction(final Connection connection, final long id,
                                             final String newPassword, final String createBy) throws SQLException {
        final boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        boolean completed = false;
        Throwable failure = null;
        try {
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
            completed = true;
        } catch (SQLException | RuntimeException | Error e) {
            failure = e;
            completed = rollback(connection, e);
            throw e;
        } finally {
            if (completed) {
                restoreAutoCommit(connection, autoCommit, failure);
            }
        }
    }

    private boolean rollback(final Connection connection, final Throwable originalException) {
        try {
            connection.rollback();
            return true;
        } catch (SQLException e) {
            originalException.addSuppressed(e);
            return false;
        }
    }

    private void restoreAutoCommit(final Connection connection, final boolean autoCommit,
                                   final Throwable originalException) throws SQLException {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            if (originalException == null) {
                throw e;
            }
            originalException.addSuppressed(e);
        }
    }

    private void releaseConnection(final Connection connection, final Throwable originalException) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (RuntimeException e) {
            if (originalException == null) {
                throw e;
            }
            originalException.addSuppressed(e);
        }
    }
}
