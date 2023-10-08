package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        try (final var conn = dataSource.getConnection()) {
            try {
                conn.setAutoCommit(false);

                return userDao.findById(conn, id);
            } catch (Exception e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
            conn.commit();
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
        throw new RuntimeException();
    }

    public void insert(final User user) {
        try (final var conn = dataSource.getConnection()) {
            try {
                conn.setAutoCommit(false);

                userDao.insert(conn, user);
            } catch (Exception e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
            conn.commit();
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (final var conn = dataSource.getConnection()) {
            try {
                conn.setAutoCommit(false);

                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(conn, user);
                userHistoryDao.log(conn, new UserHistory(user, createBy));
            } catch (Exception e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
            conn.commit();
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }
}
