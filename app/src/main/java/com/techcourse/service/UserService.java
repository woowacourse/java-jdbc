package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id: " + id));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        DataAccessException failure = null;

        try {
            connection.setAutoCommit(false);
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(connection, e);
            failure = new DataAccessException(e);
            throw failure;
        } finally {
            release(connection, failure);
        }
    }

    private void rollback(Connection connection, Exception error) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            error.addSuppressed(e);
        }
    }

    private void release(Connection connection, DataAccessException failure) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        try {
            try {
                connection.setAutoCommit(true);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            if (failure != null) {
                failure.addSuppressed(e);
                return;
            }
            throw new DataAccessException(e);
        }
    }
}
