package com.techcourse.service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(Connection connection, User user) {
        userDao.insert(connection, user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = getConnectionWithTransaction(dataSource);

        try {
            User user = findById(id);
            updateUserPasswordAndLogHistory(connection, user, newPassword, createBy);
            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction(connection);
        } finally {
            closeConnection(connection);
        }
    }

    private Connection getConnectionWithTransaction(DataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void updateUserPasswordAndLogHistory(Connection connection, User user, String newPassword, String createBy) throws SQLException {
        user.changePassword(newPassword);
        userDao.update(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, createBy));
    }

    private void rollbackTransaction(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }
}
