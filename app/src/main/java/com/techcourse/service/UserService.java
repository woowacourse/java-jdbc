package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private DataSource dataSource;

    public UserService(DataSource datasource, UserDao userDao, UserHistoryDao userHistoryDao) {
        this.dataSource = datasource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            doChangePassword(id, newPassword, createBy, connection);
        } catch (DataAccessException e) {
            connection.rollback();
            throw new DataAccessException(e);
        } finally {
            connection.close();
        }
    }

    private void doChangePassword(long id, String newPassword, String createBy, Connection connection) {
        User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, createBy));
    }
}
