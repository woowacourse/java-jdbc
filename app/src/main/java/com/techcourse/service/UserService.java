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
        try (final var connection = dataSource.getConnection()) {
            changePasswordInTransaction(connection, id, newPassword, createBy);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
    private void changePasswordInTransaction(final Connection connection, final long id,
                                             final String newPassword, final String createBy) throws SQLException {
        connection.setAutoCommit(false);
        try {
            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            rollback(connection, e);
            throw e;
        }
    }

    private void rollback(final Connection connection, final Exception originalException) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            originalException.addSuppressed(e);
        }
    }
}
