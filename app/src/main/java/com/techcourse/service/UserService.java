package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
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
        try (final var connection = dataSource.getConnection()) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            Throwable failure = null;
            boolean canRestore = true;
            try {
                final var user = userDao.findById(connection, id);
                if (user == null) {
                    throw new IllegalArgumentException("User not found: " + id);
                }
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
            } catch (SQLException | RuntimeException | Error exception) {
                failure = exception;
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    canRestore = false;
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            } finally {
                try {
                    if (canRestore) {
                        connection.setAutoCommit(autoCommit);
                    }
                } catch (SQLException restoreFailure) {
                    if (failure != null) {
                        failure.addSuppressed(restoreFailure);
                    } else {
                        throw restoreFailure;
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to change password transactionally", exception);
        }
    }
}
