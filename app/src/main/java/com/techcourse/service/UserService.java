package com.techcourse.service;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.TransactionJdbcTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final JdbcTemplate jdbcTemplate) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate is not initialized");
        }
        User user = findById(id);
        user.changePassword(newPassword);
        ((TransactionJdbcTemplate) jdbcTemplate).executeInTransaction((connection) -> {
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        });
    }
}
