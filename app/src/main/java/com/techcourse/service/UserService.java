package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    // TODO 4단계에서 트랜잭션 로직 분리하기
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User user = userDao.findById(conn, id);
                user.changePassword(newPassword);
                userDao.update(conn, user);
                userHistoryDao.log(conn, new UserHistory(user, createBy));
                conn.commit();
            } catch (Exception e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection conn, Exception originalException) {
        try {
            conn.rollback();
        } catch (SQLException rollbackEx) {
            log.error("Rollback failed", rollbackEx);
            originalException.addSuppressed(rollbackEx);
        }
    }
}
