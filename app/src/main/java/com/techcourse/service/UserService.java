package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

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
        try (Connection connection = dataSource.getConnection()) {
            changePasswordInTransaction(connection, id, newPassword, createBy);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void changePasswordInTransaction(final Connection connection, final long id, final String newPassword,
                                              final String createBy) {
        try {
            connection.setAutoCommit(false);
            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            if (userDao.update(connection, user) != 1) {
                throw new DataAccessException("회원을 갱신하지 못했습니다: " + id);
            }
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (RuntimeException e) {
            rollback(connection, e);
            throw e;
        } catch (SQLException e) {
            rollback(connection, e);
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
}
