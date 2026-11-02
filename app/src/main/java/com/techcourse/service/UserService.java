package com.techcourse.service;

import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final TransactionTemplate transactionTemplate;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(
            final TransactionTemplate transactionTemplate,
            final UserDao userDao,
            final UserHistoryDao userHistoryDao
    ) {
        this.transactionTemplate = transactionTemplate;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.execute(connection -> {
            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }
}
