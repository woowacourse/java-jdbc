package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.Connection;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public void insert(final User user) {
        try (final Connection connection = dataSource.getConnection()) {
            userDao.insert(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, "system"));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        try (final Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
            } catch (final Exception e) {
                connection.rollback();
                throw new RuntimeException(e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }
}
