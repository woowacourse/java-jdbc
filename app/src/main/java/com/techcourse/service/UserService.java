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
import org.springframework.jdbc.CannotGetJdbcConnectionException;
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
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);
            User user = findById(id);
            user.changePassword(newPassword);

            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            conn.commit();
        } catch (RuntimeException | SQLException e) {
            rollback(conn, e);
            throw new DataAccessException(e);
        } finally {
            close(dataSource);
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

    private void close(DataSource dataSource) {
        try {
            DataSourceUtils.releaseConnection(dataSource);
        } catch (CannotGetJdbcConnectionException e) {
        }
    }
}
