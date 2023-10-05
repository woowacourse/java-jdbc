package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.exception.ConnectionException;
import org.springframework.jdbc.exception.RollbackFailException;

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
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);

            userDao.insert(connection, user);

            connection.commit();
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        } catch (DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e.getMessage());
        } finally {
            close(connection);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);

            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        } catch (DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e.getMessage());
        } finally {
            close(connection);
        }
    }

    private Connection getConnection() {
        try {
            return DataSourceConfig.getInstance().getConnection();
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RollbackFailException(e.getMessage());
        }
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }
}
