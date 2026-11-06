package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

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
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            userService.changePassword(id, newPassword, createdBy);
            return;
        }

        final var connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            executeTransaction(connection, id, newPassword, createdBy);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException releaseFailure) {
                if (failure != null) {
                    failure.addSuppressed(releaseFailure);
                } else {
                    throw releaseFailure;
                }
            }
        }
    }

    private void executeTransaction(final Connection connection, final long id,
                                    final String newPassword, final String createdBy) {
        final boolean autoCommit;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to start password change transaction", e);
        }

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable failure = null;
        boolean completed = false;
        try {
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
            completed = true;
        } catch (SQLException e) {
            final var transactionFailure = new DataAccessException("Failed to commit password change", e);
            failure = transactionFailure;
            completed = rollback(connection, transactionFailure);
            throw transactionFailure;
        } catch (RuntimeException | Error e) {
            failure = e;
            completed = rollback(connection, e);
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            if (completed) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException e) {
                    if (failure != null) {
                        failure.addSuppressed(e);
                    } else {
                        throw new DataAccessException("Failed to restore autoCommit", e);
                    }
                }
            }
        }
    }

    private boolean rollback(final Connection connection, final Throwable failure) {
        try {
            connection.rollback();
            return true;
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
            return false;
        }
    }
}
