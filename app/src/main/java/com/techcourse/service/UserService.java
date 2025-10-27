package com.techcourse.service;

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

    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        final var user = findById(id);
        user.changePassword(newPassword);
        Connection connection = userDao.getConnection();
        try {
            userDao.startTransaction(connection);
            userDao.updateWithTransaction(user, connection);
            userHistoryDao.logWithTransaction(new UserHistory(user, createBy), connection);
            userDao.commitTransaction(connection);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }
}
