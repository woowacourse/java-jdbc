package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.transaction.Transaction;

public class UserService {

    private static final Transaction TRANSACTION = new Transaction(DataSourceConfig.getInstance());

    private final UserDao userDao;

    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao,
                       final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return TRANSACTION.run(connection -> userDao.findById(id));
    }

    public void insert(final User user) {
        TRANSACTION.run(connection -> {
            userDao.insert(user);
            return null;
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        TRANSACTION.run(connection -> {
            final User user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            System.out.println(" = ");
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            return null;
        });
    }
}
