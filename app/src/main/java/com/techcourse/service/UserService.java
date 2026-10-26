package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

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
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("Nested transactions are not supported");
        }
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable failure = null;
        try {
            changePassword(connection, id, newPassword, createBy);
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
            final String createBy
    ) throws SQLException {
        final var originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        boolean transactionCompleted = false;
        Throwable failure = null;
        try {
            final var user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
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
