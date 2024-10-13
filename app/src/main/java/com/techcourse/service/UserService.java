package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final User user = findById(id);
        user.changePassword(newPassword);

        try (final Connection connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            updatePassword(createBy, connection, user);
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void updatePassword(String createBy, Connection connection, User user) throws SQLException {
        try {
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        } catch (SQLException e) {
            connection.rollback();
            throw new DataAccessException(e);
        }
    }
}
