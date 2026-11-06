package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
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
        final Connection connection = getConnection();
        final boolean autoCommit = getAutoCommit(connection);
        try {
            connection.setAutoCommit(false);

            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (SQLException e) {
            rollback(connection, e);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(connection, e);
            throw e;
        } finally {
            restoreAutoCommit(connection, autoCommit);
            close(connection);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private boolean getAutoCommit(final Connection connection) {
        try {
            return connection.getAutoCommit();
        } catch (SQLException e) {
            close(connection);
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection, final Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }

    private void restoreAutoCommit(final Connection connection, final boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void close(final Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
