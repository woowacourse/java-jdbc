package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        Connection conn = null;

        try {
            DataSource dataSource = DataSourceConfig.getInstance();
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            changePassword(conn, id, newPassword, createBy);

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            close(conn);
        }
    }

    private void changePassword(Connection conn, long id, String newPassword, String createBy) throws SQLException {
        User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(conn, user);
        userHistoryDao.log(conn, new UserHistory(user, createBy));
    }

    private void rollback(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException ignored) {}
    }

    private void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ignored) {}
    }
}
