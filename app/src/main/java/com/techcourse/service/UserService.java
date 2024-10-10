package com.techcourse.service;

import com.interface21.jdbc.exception.DataQueryException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(Connection connection, long id) {
        return userDao.findById(connection, id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
    }

    public void insert(User user) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            userDao.insert(connection, user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(long id, String newPassword, String createBy) {
        doTransaction(connection -> {
            User user = findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }

    private void doTransaction(Consumer<Connection> consumer) {
        Connection connection = null;
        try {
            connection = DataSourceConfig.getInstance().getConnection();
            connection.setAutoCommit(false);

            consumer.accept(connection);

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                handleRollBack(connection);
            }
            throw new DataQueryException(e.getMessage(), e);
        }
    }

    private void handleRollBack(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            throw new DataQueryException(rollbackEx.getMessage(), rollbackEx);
        }
    }
}
