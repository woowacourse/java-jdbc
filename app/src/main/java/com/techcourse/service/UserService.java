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
        final var user = findById(id);
        user.changePassword(newPassword);

        final Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);

            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));

            conn.commit();
        } catch (final SQLException e) {
            rollback(conn);
            throw new DataAccessException(e.getMessage());
        }
    }

    private Connection getConnection() {
        try {
            final DataSource dataSource = DataSourceConfig.getInstance();
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
