package com.techcourse.service;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Inject;
import com.interface21.jdbc.TransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.util.function.Consumer;

@Component
public class UserService {

    @Inject
    private UserDao userDao;

    @Inject
    private UserHistoryDao userHistoryDao;

    @Inject
    private TransactionManager transactionManager;

    private UserService() {
    }

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, TransactionManager transactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public User findByAccount(String account) {
        return userDao.findByAccount(account);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        User user = findById(id);
        user.changePassword(newPassword);
        Consumer<Connection> consumer = (connection) -> {
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        };
        transactionManager.execute(consumer);
    }
}
