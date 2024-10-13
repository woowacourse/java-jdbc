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

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this(userDao, userHistoryDao, null);
    }

    public UserService(
            final UserDao userDao,
            final UserHistoryDao userHistoryDao,
            final TransactionManager transactionManager
    ) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.executeInTransaction(
                () -> {
                    final var user = findById(id);
                    user.changePassword(newPassword);
                    userDao.update(user);
                    userHistoryDao.log(new UserHistory(user, createBy));
                }
        );
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public User findByAccount(String account) {
        return userDao.findByAccount(account);
    }
}
