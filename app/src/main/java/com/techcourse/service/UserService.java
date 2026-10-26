package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao,
                       final UserHistoryDao userHistoryDao,
                       final DataSource dataSource) {
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

    public void changePassword(final long id,
                               final String newPassword,
                               final String createBy) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                final var user = userDao.findById(connection, id);

                if (user == null) {
                    throw new IllegalArgumentException("존재하지 않는 사용자: " + id);
                }

                user.changePassword(newPassword);

                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));

                connection.commit();

            } catch (SQLException e) {
                rollback(connection, e);
                throw new DataAccessException(e);

            } catch (RuntimeException | Error e) {
                rollback(connection, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection connection, Throwable originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
