package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
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
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. id=" + id));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            final Connection conn = DataSourceUtils.getConnection(dataSource);
            try (TransactionCleanup cleanup = () -> releaseTransactionConnection(conn)) {
                conn.setAutoCommit(false);
                TransactionSynchronizationManager.bindResource(dataSource, conn);

                try {
                    final var user = findById(id);
                    user.changePassword(newPassword);
                    userDao.update(user);
                    userHistoryDao.log(new UserHistory(user, createBy));
                    conn.commit();
                } catch (SQLException | RuntimeException | Error e) {
                    rollback(conn, e);
                    throw e;
                }
            }
        } catch (SQLException | CannotGetJdbcConnectionException e) {
            throw new DataAccessException(e);
        }
    }

    private void releaseTransactionConnection(final Connection conn) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(conn, dataSource);
    }

    private void rollback(final Connection conn, final Throwable original) {
        try {
            conn.rollback();
        } catch (SQLException rollbackException) {
            original.addSuppressed(rollbackException);
        }
    }

    @FunctionalInterface
    private interface TransactionCleanup extends AutoCloseable {
        @Override
        void close();
    }
}
