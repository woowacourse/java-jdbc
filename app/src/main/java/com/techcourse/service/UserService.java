package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        try {
            Connection connection = dataSource.getConnection();
            return userDao.findById(connection, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(final User user) {
        try {
            Connection connection = dataSource.getConnection();
            userDao.insert(connection, user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            changePasswordWithTransaction(id, newPassword, createBy);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void changePasswordWithTransaction(final long id, final String newPassword,
                                               final String createBy) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        try {
            User user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (Throwable e) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
