package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.core.TransactionExecutor;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionExecutor transactionExecutor;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final TransactionExecutor transactionExecutor) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionExecutor = transactionExecutor;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        transactionExecutor.execute(transaction ->
                userDao.insert(transaction, user)
        );
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionExecutor.execute(transaction -> {
            final var user = findById(id);
            user.changePassword(newPassword);

            userHistoryDao.log(transaction, new UserHistory(user, createBy));
            userDao.update(transaction, user);
        });
    }
}
