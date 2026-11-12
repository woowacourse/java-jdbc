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

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, DataSource dataSource) {
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
        try (final var connection = DataSourceUtils.getConnection(dataSource)) {
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
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection, final Throwable originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
