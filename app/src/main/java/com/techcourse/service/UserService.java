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

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(
            final UserDao userDao,
            final UserHistoryDao userHistoryDao,
            final DataSource dataSource
    ) {
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
        Throwable failure = null;
        try {
            connection.setAutoCommit(false);

            try {
                final var user = userDao.findById(id);
                user.changePassword(newPassword);
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, createBy));

                connection.commit();
            } catch (Throwable exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
        } catch (SQLException exception) {
            final var dataAccessException = new DataAccessException(exception);
            failure = dataAccessException;
            throw dataAccessException;
        } catch (Throwable exception) {
            failure = exception;
            throw exception;
        } finally {
            releaseConnection(connection, failure);
        }
    }

    private void releaseConnection(Connection connection, Throwable failure) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (RuntimeException closeException) {
            if (failure == null) {
                throw closeException;
            }
            failure.addSuppressed(closeException);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
