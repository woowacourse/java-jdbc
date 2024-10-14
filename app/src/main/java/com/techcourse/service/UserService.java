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
        return transactionManager.getResultInTransaction(() -> userDao.findById( id));
    }

    public void insert(User user) {
        transactionManager.executeInTransaction(() -> userDao.insert( user));
    }

    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.getResultInTransaction(() -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update( user);
            return userHistoryDao.log(new UserHistory(user, createBy));
        });
    }
}
