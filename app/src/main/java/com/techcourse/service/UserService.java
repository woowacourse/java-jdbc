package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

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
        boolean committed = false;
        try (final var connection = dataSource.getConnection()) {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                final var user = userDao.findById(connection, id);
                if (user == null) {
                    throw new NoSuchElementException("User not found, userId=" + id);
                }
                user.changePassword(newPassword);
                userDao.updatePassword(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
                committed = true;
            } catch (final SQLException | RuntimeException | Error e) {
                rollbackAndRestoreAutoCommit(connection, autoCommit, e);
                throw e;
            }
            connection.setAutoCommit(autoCommit);
        } catch (final SQLException e) {
            if (committed) {
                throw new TransactionCleanupException("Password change committed, but connection cleanup failed, userId=" + id, e);
            }
            throw new DataAccessException("Failed to change password, userId=" + id, e);
        }
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
