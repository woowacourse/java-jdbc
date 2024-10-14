package com.techcourse.service;

import com.interface21.transaction.support.TransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.Connection;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(final Connection connection, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = new TransactionManager(connection);
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final Connection connection, final User user) {
        userDao.insert(connection, user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.execute(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }
}
