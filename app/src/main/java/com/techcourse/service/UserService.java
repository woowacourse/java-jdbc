package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.exception.UserNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            connection.setAutoCommit(false);

            final User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (final Exception exception) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                throw exception;
            } catch (final SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (final SQLException ignored) {}
        }
    }

    public User findByAccount(final String account) {
        return userDao.findByAccount(account)
                .orElseThrow(UserNotFoundException::new);
    }
}
