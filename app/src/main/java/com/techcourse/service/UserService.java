package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.interface21.dao.DataAccessException;
import javax.sql.DataSource;
import java.sql.SQLException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this(DataSourceConfig.getInstance(), userDao, userHistoryDao);
    }

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
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
        try (var connection = dataSource.getConnection()) {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            Throwable failure = null;
            try {
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                failure = e;
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw e;
            } finally {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException restoreFailure) {
                    if (failure != null) {
                        failure.addSuppressed(restoreFailure);
                    } else {
                        throw restoreFailure;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to change password transactionally", e);
        }
    }
}
