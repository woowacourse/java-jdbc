package com.techcourse.service;

import com.interface21.transaction.support.TransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(
            UserDao userDao,
            UserHistoryDao userHistoryDao,
            TransactionManager transactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User findById(long id) {
        return transactionManager.performTransaction(() -> userDao.findById(id));
    }

    public void insert(User user) {
        transactionManager.performTransaction(() -> userDao.insert(user));
    }

    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.performTransaction(() -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        });
    }
}
