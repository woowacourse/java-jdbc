package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeInTransaction(() -> {
            final var user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        });
    }

    private void executeInTransaction(final Runnable action) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            beginAndRun(connection, action);
        } catch (SQLException e) {
            failure = e;
            throw new DataAccessException(e);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            release(connection, failure);
        }
    }

    private void beginAndRun(final Connection connection, final Runnable action) throws SQLException {
        final boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            runInTransaction(connection, action, autoCommit);
        } finally {
            // Unbind before release so that DataSourceUtils closes the connection instead of keeping it open.
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void runInTransaction(final Connection connection, final Runnable action, final boolean autoCommit)
            throws SQLException {
        try {
            action.run();
            connection.commit();
        } catch (SQLException | RuntimeException | Error failure) {
            rollback(connection, autoCommit, failure);
            throw failure;
        }
        // Cleanup failures after a successful commit must not trigger another rollback.
        connection.setAutoCommit(autoCommit);
    }

    private void rollback(final Connection connection, final boolean autoCommit, final Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
            // Restoring autoCommit after a failed rollback could commit pending changes.
            return;
        }
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException restoreFailure) {
            failure.addSuppressed(restoreFailure);
        }
    }

    private void release(final Connection connection, final Throwable failure) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (CannotGetJdbcConnectionException closeFailure) {
            if (failure == null) {
                throw closeFailure;
            }
            failure.addSuppressed(closeFailure.getCause());
        }
    }
}