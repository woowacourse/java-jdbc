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
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        var resourceBound = false;
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            resourceBound = true;

            final var user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            connection.commit();
        } catch (SQLException e) {
            rollback(connection, e);
            throw new DataAccessException("Failed to change password", e);
        } catch (RuntimeException | Error e) {
            rollback(connection, e);
            throw e;
        } finally {
            if (resourceBound) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(final Connection connection, final Throwable cause) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }
}
