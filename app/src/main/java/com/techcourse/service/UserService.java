package com.techcourse.service;

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
        try (final var conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(conn, user);
                userHistoryDao.log(conn, new UserHistory(user, createBy));

                conn.commit();
            } catch (DataAccessException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    throw new DataAccessException("Rollback Failed", rollbackException);
                }
                throw new DataAccessException("Transaction failed and rolled back", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Connection failed", e);
        }
    }
}
