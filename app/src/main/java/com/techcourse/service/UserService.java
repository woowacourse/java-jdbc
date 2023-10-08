package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.core.TransactionManager;

public class UserService {

    private final TransactionManager transactionManager;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(
            final TransactionManager transactionManager,
            final UserDao userDao,
            final UserHistoryDao userHistoryDao) {
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
        final var user = findById(id);
        user.changePassword(newPassword);
        transactionManager.save(
                (connection, entity) -> {
                    userDao.update(connection, entity);
                    userHistoryDao.log(connection, new UserHistory(entity, createBy));
                },
                user);
    }
}
