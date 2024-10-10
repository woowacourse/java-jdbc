package com.techcourse.service;

import com.interface21.jdbc.datasource.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

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
        return transactionManager.transaction((connection) -> {
            return userDao.findById2(connection, id);
        });
    }

    public void insert(final User user) {
        transactionManager.transaction((connection) -> {
            userDao.insert2(connection, user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.transaction((connection) -> {
                    final var user = findById(id);
                    user.changePassword(newPassword);
                    userDao.update2(connection, user);
                    userHistoryDao.log2(connection, new UserHistory(user, createBy));
                }
        );
    }
}
