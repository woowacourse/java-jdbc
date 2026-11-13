package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("A transaction is already active for this DataSource");
        }

        Connection connection = null;
        Throwable failure = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

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
        } catch (SQLException | CannotGetJdbcConnectionException e) {
            final var dataAccessException = new DataAccessException(e);
            failure = dataAccessException;
            throw dataAccessException;
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            if (connection != null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                releaseConnection(connection, failure);
            }
        }
    }

    private void rollback(Connection connection, Throwable originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }

    private void releaseConnection(Connection connection, Throwable originalException) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            if (originalException != null) {
                originalException.addSuppressed(e);
                return;
            }
            throw new DataAccessException(e);
        }
    }
}
