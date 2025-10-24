package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
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

        final var user = findById(id);
        user.changePassword(newPassword);
        try (Connection connection = userDao.getConnection()) {
            try {
                connection.setAutoCommit(false);

                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));

                connection.commit();
            } catch (RuntimeException exception) {
                try {
                    connection.rollback();
                    throw exception;
                } catch (SQLException rollbackException) {
                    throw new DataAccessException(rollbackException.getMessage());
                }
            }
        } catch (SQLException connectionException) {
            throw new DataAccessException(connectionException.getMessage());
        }
    }
}
