package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("id 값으로 해당하는 User 를 찾을 수 없습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        Connection connection = null;
        try {
            final DataSource dataSource = DataSourceConfig.getInstance();
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (final SQLException | DataAccessException e) {
            rollback(connection);
            log.error("SQLException occurred");
            throw new DataAccessException(e);
        } finally {
            release(connection);
        }
    }

    private void rollback(final Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (final SQLException ex) {
                log.error("rollback callback");
                throw new DataAccessException(ex);
            }
        }
    }

    private void release(final Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (final SQLException e) {
                log.error("Cannot close Connection");
                throw new DataAccessException(e);
            }
        }
    }
}
