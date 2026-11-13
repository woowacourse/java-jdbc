package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final DataSource dataSource) {
        this(new UserDao(dataSource), new UserHistoryDao(dataSource), dataSource);
    }

    UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
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
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("현재 스레드에 이미 트랜잭션 연결이 등록되어 있습니다.");
        }
        final var connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                updatePasswordAndLogHistory(id, newPassword, createBy);
                connection.commit();
            } catch (SQLException | RuntimeException | Error exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            final var transactionFailure = new DataAccessException("비밀번호 변경 트랜잭션에 실패했습니다.", exception);
            failure = transactionFailure;
            throw transactionFailure;
        } catch (RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            releaseConnection(connection, failure);
        }
    }

    private void updatePasswordAndLogHistory(final long id, final String newPassword, final String createBy) {
        final var user = userDao.findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }

    private void rollback(final Connection connection, final Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException | RuntimeException | Error rollbackFailure) {
            if (failure != rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
        }
    }

    private void releaseConnection(final Connection connection, final Throwable failure) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (RuntimeException | Error releaseFailure) {
            if (failure == null) {
                throw new PasswordChangeCommittedException(releaseFailure);
            }
            if (failure != releaseFailure) {
                failure.addSuppressed(releaseFailure);
            }
        }
    }
}
