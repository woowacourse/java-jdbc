package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.service.transaction.TransactionManager;

public class UserService {

    private final TransactionManager transactionManager;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final TransactionManager transactionManager, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.transactionManager = transactionManager;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return transactionManager.transaction(connection -> {
            return userDao.findById(connection, id);
        });
    }

    public void insert(final User user) {
        transactionManager.transaction(connection -> {
            userDao.insert(connection, user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.transaction(connection -> {
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }
}
