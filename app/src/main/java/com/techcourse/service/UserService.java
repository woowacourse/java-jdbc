package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.transaction.TransactionExecutor;

import java.util.Optional;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionExecutor transactionExecutor;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final TransactionExecutor transactionExecutor) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionExecutor = transactionExecutor;
    }

    public Optional<User> findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionExecutor.execute(() -> {
            final User user = userDao.findById(id).orElseThrow();
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        });
    }
}
