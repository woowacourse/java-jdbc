package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public void insert(final User user) {
        final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            userDao.insert(user);
            userHistoryDao.log(new UserHistory(user, "system"));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) throws DataAccessException {
        final var user = findById(id);
        user.changePassword(newPassword);
        final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            connection.commit();
        } catch (final Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }
}
