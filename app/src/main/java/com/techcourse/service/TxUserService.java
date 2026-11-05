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
            throw new IllegalStateException("A transaction is already active for this DataSource");
        }

        final var connection = DataSourceUtils.getConnection(dataSource);
        boolean bound = false;
        Throwable failure = null;
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            bound = true;

            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw new DataAccessException(exception);
            } catch (RuntimeException | Error exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            final var translated = new DataAccessException(exception);
            failure = translated;
            throw translated;
        } catch (RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            Throwable cleanupFailure = null;
            try {
                if (bound) {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            } catch (RuntimeException | Error cleanupException) {
                cleanupFailure = cleanupException;
            }

            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException | Error cleanupException) {
                if (cleanupFailure != null) {
                    cleanupFailure.addSuppressed(cleanupException);
                } else {
                    cleanupFailure = cleanupException;
                }
            }

            if (cleanupFailure != null) {
                if (failure != null) {
                    failure.addSuppressed(cleanupFailure);
                } else if (cleanupFailure instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                } else {
                    throw (Error) cleanupFailure;
                }
            }
        }
    }

    private static void rollback(final Connection connection, final Throwable cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }
}
