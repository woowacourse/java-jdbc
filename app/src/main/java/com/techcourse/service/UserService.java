package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.SqlExecutionException;
import com.interface21.jdbc.core.TransactionCallback;
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

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);

            final var user = userDao.findById(id);

            user.changePassword(newPassword);
            userDao.update(user);

            userHistoryDao.log(new UserHistory(user, createBy));
            connection.commit();
        } catch (Exception e) {
            rollbackAndThrow(connection, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollbackAndThrow(final Connection connection, final Exception originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            originalException.addSuppressed(rollbackEx);
        }
        throw new SqlExecutionException(originalException);
    }
}
