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

    public void changePassword(final long id, final String newPassword, final String createdBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("이미 진행 중인 트랜잭션이 있습니다.");
        }

        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean transactionStarted = false;
        boolean bound = false;
        Throwable failure = null;

        try {
            connection.setAutoCommit(false);
            transactionStarted = true;
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            bound = true;

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createdBy));

            connection.commit();
        } catch (SQLException | RuntimeException e) {
            failure = e;
            if (transactionStarted) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new DataAccessException("비밀번호 변경 트랜잭션에 실패했습니다.", e);
        } finally {
            if (bound) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException releaseException) {
                if (failure == null) {
                    throw releaseException;
                }
                failure.addSuppressed(releaseException);
            }
        }
    }
}
