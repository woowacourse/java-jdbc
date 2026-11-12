package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final UserService userService) {
        this(DataSourceConfig.getInstance(), userService);
    }

    TxUserService(final DataSource dataSource, final UserService userService) {
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
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, conn);
        try {
            changePasswordInTransaction(conn, id, newPassword, createdBy);
        } catch (RuntimeException e) {
            releaseConnection(conn, e);
            throw e;
        }
        releaseConnection(conn);
    }

    private void changePasswordInTransaction(final Connection conn, final long id, final String newPassword,
                                             final String createdBy) {
        try {
            conn.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            conn.commit();
        } catch (SQLException e) {
            rollback(conn, e);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(conn, e);
            throw e;
        }
    }

    private void rollback(final Connection conn, final Exception cause) {
        try {
            conn.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }

    private void releaseConnection(final Connection conn) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(conn, dataSource);
    }

    private void releaseConnection(final Connection conn, final RuntimeException cause) {
        try {
            releaseConnection(conn);
        } catch (RuntimeException releaseException) {
            cause.addSuppressed(releaseException);
        }
    }
}
