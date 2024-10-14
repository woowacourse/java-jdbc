package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.DataQueryException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);

            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            connection.commit();
        } catch (Exception e) {
            handleException(connection);
            if (e instanceof SQLException) {
                throw new DataQueryException(e.getMessage(), e);
            }
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void handleException(Connection connection) {
        if (connection != null) {
            handleRollBack(connection);
        }
    }

    private void handleRollBack(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            throw new DataQueryException(rollbackEx.getMessage(), rollbackEx);
        }
    }
}
