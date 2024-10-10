package com.techcourse.service;

import com.interface21.jdbc.core.JdbcTransactionTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final JdbcTransactionTemplate transactionTemplate;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao,
                       JdbcTransactionTemplate transactionTemplate) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionTemplate = transactionTemplate;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        transactionTemplate.executeTransactional(connection -> userDao.insert(user));
    }

    public void changePassword(long id, String newPassword, String createBy) {
        transactionTemplate.executeTransactional(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }
}
