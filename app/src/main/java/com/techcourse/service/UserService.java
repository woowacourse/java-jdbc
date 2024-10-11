package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = new TransactionManager();
    }

    public User findById(final long id) {
        return transactionManager.manage(conn -> {
            return userDao.findById(conn, id);
        });
    }

    public void insert(final User user) {
        transactionManager.manage(conn -> {
            userDao.insert(conn, user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.manage(conn -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));
        });
    }
}
