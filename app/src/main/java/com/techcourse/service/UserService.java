package com.techcourse.service;


import com.interface21.dao.DataAccessException;
import com.interface21.transaction.DataSourceTransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSourceTransactionManager transactionManager;

    public UserService(final UserDao userDao,
                       final UserHistoryDao userHistoryDao,
                       final DataSourceTransactionManager transactionManager
    ) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.bindConnection();
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);

            userHistoryDao.log(new UserHistory(user, createBy));

            transactionManager.commit();
        } catch (final Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        }
    }
}
