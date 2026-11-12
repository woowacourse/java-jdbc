package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final UserService userService) {
        this(DataSourceConfig.getInstance(), userService);
    }

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.userService = Objects.requireNonNull(userService);
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
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("A transaction is already bound to this data source");
        }

        final Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable failure = null;
        try {
            changePasswordInTransaction(connection, id, newPassword, createdBy);
        } catch (SQLException e) {
            failure = e;
            throw new DataAccessException("Failed to change password in a transaction", e);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            releaseConnection(connection, failure);
        }
    }

    private void changePasswordInTransaction(final Connection connection, final long id,
                                             final String newPassword, final String createdBy) throws SQLException {
        final boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException | RuntimeException | Error e) {
            rollback(connection, autoCommit, e);
            throw e;
        }
        connection.setAutoCommit(autoCommit);
    }

    private void rollback(final Connection connection, final boolean autoCommit, final Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
            // Restoring auto-commit after a failed rollback could commit the unfinished transaction.
            return;
        }
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException restoreFailure) {
            failure.addSuppressed(restoreFailure);
        }
    }

    private void releaseConnection(final Connection connection, final Throwable failure) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (RuntimeException releaseFailure) {
            if (failure == null) {
                throw releaseFailure;
            }
            failure.addSuppressed(releaseFailure);
        }
    }
}
