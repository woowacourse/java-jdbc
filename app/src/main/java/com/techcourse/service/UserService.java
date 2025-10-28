package com.techcourse.service;

import com.interface21.transaction.TransactionExecutor;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public class UserService {

    private final TransactionExecutor transactionExecutor;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.transactionExecutor = new TransactionExecutor(dataSource);
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return transactionExecutor.execute(() -> userDao.findById(id));
    }

    public void insert(final User user) {
        transactionExecutor.executeVoid(() ->
                userDao.insert(user)
        );
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionExecutor.executeVoid(() -> {
            final var user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        });
    }
}
