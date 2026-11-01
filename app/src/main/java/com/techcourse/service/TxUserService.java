package com.techcourse.service;

import com.techcourse.domain.User;
import com.techcourse.config.DataSourceConfig;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

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
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable failure = null;
        try {
            changePasswordInTransaction(connection, id, newPassword, createBy);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException e) {
                if (failure == null) {
                    throw e;
                }
                failure.addSuppressed(e);
            }
        }
    }

    private void changePasswordInTransaction(final Connection connection, final long id, final String newPassword,
                                              final String createBy) {
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            throw rollbackAndTranslate(connection, e);
        }
    }

    private RuntimeException rollbackAndTranslate(final Connection connection, final Exception cause) {
        rollback(connection, cause);
        if (cause instanceof SQLException sqlException) {
            return new DataAccessException(sqlException);
        }
        return (RuntimeException) cause;
    }

    private void rollback(final Connection connection, final Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }
}
