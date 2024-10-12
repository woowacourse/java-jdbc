package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (final Connection conn = dataSource.getConnection()) {
            changePasswordWithTransaction(id, newPassword, createBy, conn);
        } catch (SQLException e) {
            throw new DataAccessException("Connection failed", e);
        }
    }

    private void changePasswordWithTransaction(final long id, final String newPassword, final String createBy, final Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            changePasswordInTransaction(id, newPassword, createBy, conn);
            conn.commit();
        } catch (DataAccessException e) {
            handleRollback(conn);
            throw new DataAccessException("Transaction failed and rolled back", e);
        }
    }

    private void changePasswordInTransaction(long id, String newPassword, String createBy, Connection conn) {
        final User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(conn, user);
        userHistoryDao.log(conn, new UserHistory(user, createBy));
    }

    private void handleRollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException rollbackException) {
            throw new DataAccessException("Rollback Failed", rollbackException);
        }
    }
}
