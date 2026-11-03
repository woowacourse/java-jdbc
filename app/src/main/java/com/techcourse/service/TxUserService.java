package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(
            final UserService userService,
            final DataSource dataSource
    ) {
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
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            connection.setAutoCommit(false);

            try {
                userService.changePassword(id, newPassword, createBy);

                connection.commit();
            } catch (Throwable exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
        } catch (SQLException exception) {
            final var dataAccessException = new DataAccessException(exception);
            failure = dataAccessException;
            throw dataAccessException;
        } catch (Throwable exception) {
            failure = exception;
            throw exception;
        } finally {
            releaseConnection(connection, failure);
        }
    }

    private void releaseConnection(Connection connection, Throwable failure) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (RuntimeException closeException) {
            if (failure == null) {
                throw closeException;
            }
            failure.addSuppressed(closeException);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
