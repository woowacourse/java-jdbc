package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

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
        Connection connection = connect();
        try (connection) {
            connection.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.updateWithConnection(connection, user);
            userHistoryDao.logWithConnection(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollback(connection);
            throw new DataAccessException("Failed to commit transaction.", e);
        }
    }

    private Connection connect() {
        try {
            return DataSourceConfig.getInstance().getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to connect.", e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to rollback transaction.", e);
        }
    }
}
