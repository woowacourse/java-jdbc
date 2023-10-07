package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.transaction.TransactionManager;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = new TransactionManager(DataSourceConfig.getInstance());
    }

    public User findById(final long id) {
        return transactionManager.execute(connection -> userDao.findById(connection, id));
    }

    public void insert(final User user) {
        transactionManager.execute(connection -> {
            userDao.insert(connection, user);
            return null;
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.execute(connection -> {
                    final var user = userDao.findById(connection, id);
                    user.changePassword(newPassword);
                    userDao.update(connection, user);
                    userHistoryDao.log(connection, new UserHistory(user, createBy));
                    return null;
                }
        );
    }
}
