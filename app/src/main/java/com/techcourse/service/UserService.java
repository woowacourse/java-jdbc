package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            final var user = userDao.findById(connection, id);

            connection.commit();

            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(final User user) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            userDao.insert(connection, user);

            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy)); //커넥션 A

            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
