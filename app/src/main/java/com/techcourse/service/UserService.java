package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

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
        log.info("[UserService] changePassword: id={}, newPassword={}, createBy={}", id, newPassword, createBy);
        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection connection = getConnection(dataSource);
        setAutoCommit(connection, false);

        final User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(connection, user);
        userHistoryDao.create(connection, new UserHistory(user, createBy));

        commitAndRollbackAutoCommit(connection, true);
    }

    private Connection getConnection(final DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setAutoCommit(final Connection connection, final boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void commitAndRollbackAutoCommit(final Connection connection, final boolean autoCommit) {
        try {
            connection.commit();
            setAutoCommit(connection, autoCommit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
