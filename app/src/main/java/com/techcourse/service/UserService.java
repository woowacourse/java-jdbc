package com.techcourse.service;

import com.interface21.jdbc.core.JdbcTransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final JdbcTransactionManager jdbcTransactionManager;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, JdbcTransactionManager jdbcTransactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.jdbcTransactionManager = jdbcTransactionManager;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        jdbcTransactionManager.execute((connection) -> {
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }
}
