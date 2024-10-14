package com.techcourse.service;

import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionTemplate transactionTemplate;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao,
                       final TransactionTemplate transactionTemplate) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionTemplate = transactionTemplate;
    }

    public User findById(final long id) {
        return transactionTemplate.execute((connection) -> userDao.findById(id));
    }

    public void insert(final User user) {
        transactionTemplate.executeWithoutResult((connection) -> userDao.insert(user));
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.executeWithoutResult((connection) -> {
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        });
    }
}
