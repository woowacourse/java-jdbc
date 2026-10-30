package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable failure = null;
        try {
            changePasswordInTransaction(connection, id, newPassword, createBy);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException e) {
                if (failure == null) {
                    throw e;
                }
                failure.addSuppressed(e);
            }
        }
    }

    private void changePasswordInTransaction(final Connection connection, final long id, final String newPassword,
                                              final String createBy) {
        try {
            connection.setAutoCommit(false);
            updatePasswordAndLogHistory(id, newPassword, createBy);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            throw rollbackAndTranslate(connection, e);
        }
    }

    private void updatePasswordAndLogHistory(final long id, final String newPassword, final String createBy) {
        final var user = userDao.findById(id);
        user.changePassword(newPassword);
        if (userDao.update(user) != 1) {
            throw new DataAccessException("회원을 갱신하지 못했습니다: " + id);
        }
        userHistoryDao.log(new UserHistory(user, createBy));
    }

    private RuntimeException rollbackAndTranslate(final Connection connection, final Exception cause) {
        rollback(connection, cause);
        if (cause instanceof SQLException sqlException) {
            return new DataAccessException(sqlException);
        }
        return (RuntimeException) cause;
    }

    private void rollback(final Connection connection, final Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }
}
