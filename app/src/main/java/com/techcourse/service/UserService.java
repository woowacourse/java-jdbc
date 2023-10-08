package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
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
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return userDao.findById(connection, id);
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        } finally {
            close(connection);
        }

    }

    public void insert(User user) throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            userDao.insert(connection, user);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        } finally {
            close(connection);
        }
    }

    public void changePassword(long id, String newPassword, String createBy) throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        } finally {
            close(connection);
        }
    }

    private void close(Connection connection){
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
