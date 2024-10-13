package com.techcourse.service;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.transaction.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final TransactionManager transactionManager;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(
            final TransactionManager transactionManager,
            final UserDao userDao,
            final UserHistoryDao userHistoryDao
    ) {
        this.transactionManager = transactionManager;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        log.info("[UserService] changePassword: id={}, newPassword={}, createBy={}", id, newPassword, createBy);
        final DataSource dataSource = DataSourceConfig.getInstance();

        transactionManager.beginTransaction(dataSource, connection -> {
            final User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.create(connection, new UserHistory(user, createBy));
        });
    }
}
