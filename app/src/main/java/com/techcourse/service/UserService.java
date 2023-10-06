package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;


    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = initializeConnection(dataSource);
        try {
            User user = userDao.findById(connection, id);

            commit(connection);
            return user;
        } catch (Exception exception) {
            rollback(connection);
            throw new DataAccessException(exception);
        } finally {
            close(connection);
        }
    }

    public void insert(final User user) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = initializeConnection(dataSource);
        try {
            userDao.insert(connection, user);

            commit(connection);
        } catch (Exception exception) {
            rollback(connection);
            throw new DataAccessException(exception);
        } finally {
            close(connection);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = initializeConnection(dataSource);
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            commit(connection);
        } catch (Exception exception) {
            rollback(connection);
            throw new DataAccessException(exception);
        } finally {
            close(connection);
        }
    }

    private Connection initializeConnection(final DataSource dataSource) {
        try {
            final Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    private void commit(final Connection connection) throws SQLException {
        connection.commit();
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    private void close(final Connection connection) {
        try {
            connection.close();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}
