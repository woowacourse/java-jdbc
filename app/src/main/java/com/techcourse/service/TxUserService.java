package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final UserService userService) {
        this(DataSourceConfig.getInstance(), userService);
    }

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        try {
            final Connection conn = DataSourceUtils.getConnection(dataSource);
            try (TransactionCleanup cleanup = () -> releaseTransactionConnection(conn)) {
                conn.setAutoCommit(false);
                TransactionSynchronizationManager.bindResource(dataSource, conn);

                try {
                    userService.changePassword(id, newPassword, createdBy);
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
