package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.transaction.support.TransactionExecutor;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionExecutor executor;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final TransactionExecutor executor) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.executor = executor;
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
        final UserHistory userHistory = new UserHistory(user, createBy);

        executor.execute(connection -> {
            userDao.update(connection, user);
            userHistoryDao.log(connection, userHistory);
        });
    }
}
