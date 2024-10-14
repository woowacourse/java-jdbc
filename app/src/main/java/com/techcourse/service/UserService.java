package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        Function<Connection, User> consumer = (connection) -> userDao.findById(connection, id);
        return TxManager.run(consumer);
    }

    public void insert(User user) {
        Consumer<Connection> consumer = (connection) -> {
            userDao.insert(connection, user);
        };
        TxManager.run(consumer);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        Consumer<Connection> consumer = (connection) -> {
            var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        };
        TxManager.run(consumer);
    }
}
