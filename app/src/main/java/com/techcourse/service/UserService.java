package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.ConnectionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public User findById(final Connection connection, final long id) {
        return userDao.findById(connection, id);
    }

    public User findByAccount(final String account) {
        return userDao.findByAccount(account);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;
        try {
            connection = ConnectionManager.startTransaction(DataSourceConfig.getInstance());

            final var user = findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            ConnectionManager.commitTransaction(connection);
        } catch (Exception e) {
            if (connection != null) {
                ConnectionManager.rollbackTransaction(connection);
            }
            throw new DataAccessException(e);
        }
    }
}
