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

        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (SQLException | RuntimeException | Error failure) {
                rollback(connection, failure);
                throw failure;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to change password", e);
        }
    }

    private void rollback(final Connection connection, final Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }
}
