package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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
        try (final Connection connection = dataSource.getConnection()) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            changePasswordInTransaction(connection, id, newPassword, createBy, autoCommit);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void changePasswordInTransaction(final Connection connection, final long id, final String newPassword,
                                             final String createBy, final boolean autoCommit) throws SQLException {
        try {
            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
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
}
