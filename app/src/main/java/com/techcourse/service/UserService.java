package com.techcourse.service;

import javax.sql.DataSource;
import com.interface21.transaction.support.JdbcTransactionManager;
import com.interface21.transaction.support.JdbcTransactionTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final JdbcTransactionTemplate jdbcTransactionTemplate;

    public UserService(
            final UserDao userDao,
            final UserHistoryDao userHistoryDao,
            final DataSource dataSource
    ) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.jdbcTransactionTemplate = new JdbcTransactionTemplate(new JdbcTransactionManager(dataSource));
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        jdbcTransactionTemplate.execute((transaction -> {
            final var user = userDao.findById(id, transaction);
            user.changePassword(newPassword);
            userDao.update(user, transaction);
            userHistoryDao.log(new UserHistory(user, createBy), transaction);
        }));
    }
}
