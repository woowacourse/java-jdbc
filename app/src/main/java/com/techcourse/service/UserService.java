package com.techcourse.service;

import com.interface21.transaction.TransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, TransactionManager transactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User getById(final long id) {
        return userDao.findById(id)
                .orElseThrow(IllegalArgumentException::new);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePasswordWithTransaction(final long id, final String newPassword, final String createBy) {
        transactionManager.performTransaction(connection -> changePassword(id, newPassword, createBy));
    }

    private void changePassword(final long id, final String newPassword, final String createBy) {
        final User user = getById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
