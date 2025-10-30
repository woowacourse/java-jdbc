package com.techcourse.service;

import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import java.sql.Connection;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionTemplate transactionTemplate;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final TransactionTemplate transactionTemplate) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionTemplate = transactionTemplate;
    }

    public User findById(final Connection connection, final long id) {
        return userDao.findById(connection, id);
    }

    public void insert(final Connection connection, final User user) {
        userDao.insert(connection, user);
    }

    public void changePassword(final long id, final String newPassword, final String createdBy) {
        try {
            transactionTemplate.execute(connection -> {
                final var user = findById(connection, id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createdBy));
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
