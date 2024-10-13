package com.techcourse.service;

import com.interface21.jdbc.core.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    TransactionManager transactionManager = new TransactionManager(DataSourceConfig.getInstance());

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return transactionManager.getResultInTransaction(connection -> userDao.findById(connection, id));
    }

    public void insert(User user) {
        transactionManager.executeInTransaction(connection -> userDao.insert(connection, user));
    }

    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.getResultInTransaction(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            return userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }
}
