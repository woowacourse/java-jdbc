package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            try {
                connection.setAutoCommit(false);
                final var user = userDao.findById(id);
                connection.commit();
                return user;
            } catch (DataAccessException e) {
                rollback(e, connection);
                throw new DataAccessException(e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void insert(final User user) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            try {
                connection.setAutoCommit(false);
                userDao.insert(user);
                connection.commit();
            } catch (DataAccessException e) {
                rollback(e, connection);
                throw new DataAccessException(e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            final var user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            connection.commit();
        } catch (DataAccessException e) {
            rollback(e, connection);
            throw new DataAccessException(e);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }

    private void rollback(final DataAccessException e, final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            e.addSuppressed(rollbackEx);
        }
    }
}
