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

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(IllegalArgumentException::new);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(conn, user);
            userHistoryDao.log(dataSource.getConnection(), new UserHistory(user, createBy));

            conn.commit();
        } catch (final SQLException e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            close(conn);
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException("fail rollback");
        }
    }

    private void close(final Connection conn) {
        try {
            conn.close();
        } catch (final SQLException e) {
            throw new DataAccessException("fail close");
        }
    }
}
