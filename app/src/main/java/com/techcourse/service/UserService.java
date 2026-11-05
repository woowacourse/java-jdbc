package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

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
        connection.setAutoCommit(false);
        try {
            final var user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException | RuntimeException | Error e) {
            rollback(connection, e);
            throw e;
        }
    }

    private void rollback(final Connection connection, final Throwable originalException) {
        try {
            connection.rollback();
        } catch (SQLException e) {
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
