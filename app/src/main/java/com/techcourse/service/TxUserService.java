package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
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
            throw new IllegalStateException("Transaction already active for DataSource");
        }
        final var connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                TransactionSynchronizationManager.bindResource(dataSource, connection);
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                rollback(connection, autoCommit, e);
                throw e;
            }
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            failure = new DataAccessException("Failed to change password", e);
            throw (DataAccessException) failure;
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException closeException) {
                if (failure == null) {
                    throw closeException;
                }
                failure.addSuppressed(closeException);
            }
        }
    }

    private void rollback(final Connection connection, final boolean autoCommit, final Throwable cause) {
        try {
            connection.rollback();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }
}
