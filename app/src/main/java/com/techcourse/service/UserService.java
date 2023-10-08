package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
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
        User user = findById(id);
        user.changePassword(newPassword);

        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
            conn.setAutoCommit(false);

            userDao.update(user, conn);
            userHistoryDao.log(conn, new UserHistory(user, createBy));

            conn.commit();
        } catch (RuntimeException | SQLException e) {
            rollback(conn, e);
            throw new DataAccessException(e);
        } finally {
            close(conn);
        }
    }

    private void rollback(Connection conn, Exception e) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            throw new DataAccessException("롤백 실패", e);
        }
    }

    private void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
