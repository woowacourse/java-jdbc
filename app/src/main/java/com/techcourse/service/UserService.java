package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

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
        Connection conn = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            conn.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);

            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            conn.commit();
        } catch (Exception e) {
            log.atError().log("Transaction is being rolled back", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new DataAccessException("Rollback failed: " + ex.getMessage(), ex);
                }
            }
            throw new DataAccessException("Failed to change password: " + e.getMessage(), e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    log.atError().log("Error closing connection", e);
                }
                DataSourceUtils.releaseConnection(conn, DataSourceConfig.getInstance());
            }
        }
    }
}
