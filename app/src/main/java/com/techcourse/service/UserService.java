package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                commit(connection);
            } catch (RuntimeException e) {
                rollback(e, connection);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private static void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (SQLException sqlException) {
            throw new DataAccessException(sqlException.getMessage(), sqlException);
        }
    }

    private static void rollback(final RuntimeException exception, final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException sqlException) {
            exception.addSuppressed(sqlException);
        }
        throw exception;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }
}
