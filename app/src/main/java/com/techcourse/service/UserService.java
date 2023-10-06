package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(long id) {
        try {
            Connection connection = getConnection();
            return userDao.findById(connection, id);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void insert(User user) {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);

            userDao.insert(connection, user);

            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void changePassword(long id, String newPassword, String createBy) {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);

            User user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
