package com.techcourse.service;

import com.techcourse.domain.User;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService,
                         final DataSource dataSource) {
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
    public void changePassword(final long id,
                               final String newPassword,
                               final String createdBy) {

        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean bound = false;

        try {
            connection.setAutoCommit(false);

            TransactionSynchronizationManager.bindResource(dataSource, connection);
            bound = true;

            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();

            } catch (SQLException e) {
                rollback(connection, e);
                throw new DataAccessException(e);

            } catch (RuntimeException | Error e) {
                rollback(connection, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new DataAccessException(e);

        } finally {
            if (bound) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }

            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Connection connection, Throwable originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
