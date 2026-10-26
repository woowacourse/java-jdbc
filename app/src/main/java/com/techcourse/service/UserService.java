package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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
        try (final var connection = dataSource.getConnection()) {
            final var originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            boolean transactionCompleted = false;
            Throwable failure = null;

            try {
                final var user = userDao.findById(connection, id);

                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));

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
        } catch (SQLException e) {
            throw new DataAccessException(e);
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
