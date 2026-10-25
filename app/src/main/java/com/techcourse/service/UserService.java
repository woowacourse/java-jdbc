package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
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
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("Transaction already active for dataSource");
        }
        final Connection connection = getConnection();
        Throwable failure = null;
        try {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                final var user = userDao.findById(id);
                user.changePassword(newPassword);
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, createBy));
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                rollbackAndRestoreAutoCommit(connection, autoCommit, e);
                throw e;
            }
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            failure = e;
            throw new DataAccessException(e);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException | Error cleanupFailure) {
                if (failure == null) {
                    throw cleanupFailure;
                }
                failure.addSuppressed(cleanupFailure);
            }
        }
    }

    private Connection getConnection() {
        try {
            return DataSourceUtils.getConnection(dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DataAccessException(e.getCause());
        }
    }

    private void rollbackAndRestoreAutoCommit(final Connection connection, final boolean autoCommit,
                                              final Throwable failure) {
        try {
            connection.rollback();
            // 롤백에 실패하면 남은 변경이 커밋되지 않도록 자동 커밋 설정을 복구하지 않는다.
            connection.setAutoCommit(autoCommit);
        } catch (SQLException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }
}
