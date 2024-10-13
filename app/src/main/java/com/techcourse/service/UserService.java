package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = DataSourceConfig.getInstance();
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        Connection connection = getConnection();
        try (connection) {
            connection.setAutoCommit(false);
            connection.setReadOnly(true);

            User user = userDao.findById(connection, id);

            connection.commit();
            return user;

        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    public void insert(final User user) {
        Connection connection = getConnection();
        try (connection) {
            connection.setAutoCommit(false);

            userDao.save(connection, user);

            connection.commit();

        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = getConnection();
        try (connection) {
            connection.setAutoCommit(false);

            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();

        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error("Rollback failed", e.getMessage());
            throw new DataAccessException(e);
        }
    }
}
