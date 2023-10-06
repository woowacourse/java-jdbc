package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplateException;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        try {
            Connection connection = dataSource.getConnection();
            return findByIdWithTransaction(id, connection);
        } catch (SQLException e) {
            throw new DataBaseAccessException(e.getMessage());
        }
    }

    private User findByIdWithTransaction(final long id, final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        try {
            return userDao.findById(connection, id);
        } catch (JdbcTemplateException e) {
            connection.rollback();
            throw new DataBaseAccessException(e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void insert(final User user) {
        try {
            Connection connection = dataSource.getConnection();
            insertWithTransaction(user, connection);
        } catch (SQLException e) {
            throw new DataBaseAccessException(e.getMessage());
        }
    }

    private void insertWithTransaction(final User user, final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        try {
            userDao.insert(connection, user);
        } catch (JdbcTemplateException e) {
            connection.rollback();
            throw new DataBaseAccessException(e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        try {
            Connection connection = dataSource.getConnection();
            changePasswordWithTransaction(createBy, user, connection);
        } catch (SQLException e) {
            throw new DataBaseAccessException(e.getMessage());
        }
    }

    private void changePasswordWithTransaction(
            final String createBy,
            final User user,
            final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        try {
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        } catch (JdbcTemplateException e) {
            connection.rollback();
            throw new DataBaseAccessException(e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
