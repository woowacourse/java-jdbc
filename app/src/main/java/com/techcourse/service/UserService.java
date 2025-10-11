package com.techcourse.service;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.techcourse.config.DataSourceConfig;
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

    public void changePassword(
            final long id,
            final String newPassword,
            final String createBy
    ) {
        final var connection = getConnection();
        try {
            connection.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
                throw e;
            } catch (SQLException sqlException) {
                log.error(sqlException.getMessage(), sqlException);
                throw new RuntimeException("failed to rollback transaction");
            }
        } finally {
            try {
                connection.close();
            } catch (SQLException sqlException) {
                log.error(sqlException.getMessage(), sqlException);
                throw new RuntimeException("failed to close db connection");
            }
        }
    }

    private Connection getConnection() {
        final DataSource dataSource = DataSourceConfig.getInstance();
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage(), e);
        }
    }
}
