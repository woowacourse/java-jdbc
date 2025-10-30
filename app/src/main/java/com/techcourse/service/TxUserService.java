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

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        boolean originalAutoCommit = false;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                throw new DataAccessException("Failed to rollback transaction", rollbackException);
            }
            throw new DataAccessException(e);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                throw new DataAccessException("Failed to reset autoCommit", e);
            }
            try {
                TransactionSynchronizationManager.unbindResource(dataSource);
            } catch (IllegalStateException e) {
                throw new DataAccessException("Failed to unbind resource", e);
            }
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (Exception e) {
                throw new DataAccessException("Failed to release connection", e);
            }
        }
    }

}
