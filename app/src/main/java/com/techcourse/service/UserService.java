package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + id));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        try (final var conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                userDao.update(conn, user);
                userHistoryDao.log(conn, new UserHistory(user, createBy));
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection conn, final Exception cause) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.error("Rollback failed. Original exception:", cause);
            final var rollbackException = new DataAccessException("Failed to rollback transaction", e);
            rollbackException.addSuppressed(cause);
            throw rollbackException;
        }
    }
}
