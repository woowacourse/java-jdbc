package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

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
        Connection connection = null;
        Throwable transactionFailure = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            try {
                final var user = userDao.findByIdForUpdate(id);
                user.changePassword(newPassword);
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, createBy));
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            final var exception = new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
            transactionFailure = exception;
            throw exception;
        } catch (CannotGetJdbcConnectionException e) {
            final var exception = new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e.getCause());
            transactionFailure = exception;
            throw exception;
        } catch (RuntimeException | Error e) {
            transactionFailure = e;
            throw e;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (CannotGetJdbcConnectionException e) {
                if (transactionFailure == null) {
                    throw new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e.getCause());
                }
                transactionFailure.addSuppressed(e.getCause());
            }
        }
    }
}
