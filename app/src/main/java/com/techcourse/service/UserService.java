package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;

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

        try (final var connection = getConnection()) {
            try {
                connection.setAutoCommit(false);

                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.updatePassword(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));

                connection.commit();
            } catch (final SQLException | DataAccessException e) {
                rollback(connection, e);
            }
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static void rollback(final Connection connection, final Exception e) throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
        throw new DataAccessException(e);
    }

    private Connection getConnection() throws SQLException {
        return DataSourceConfig.getInstance().getConnection();
    }
}
