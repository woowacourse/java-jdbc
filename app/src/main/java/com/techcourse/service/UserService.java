package com.techcourse.service;

import com.interface21.transaction.JdbcTransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.util.Optional;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final JdbcTransactionManager transactionManager;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, JdbcTransactionManager transactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public Optional<User> findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, String newPassword, String createBy) {
        User user = findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.changePassword(newPassword);
        transactionManager.execute(() -> {
            final int updatedCount = userDao.update(user);
            if (updatedCount == 0) {
                throw new UserNotFoundException(id);
            }
            userHistoryDao.log(new UserHistory(user, createBy));
        });
    }
}
