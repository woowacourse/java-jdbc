package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
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
        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);

            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (final Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
