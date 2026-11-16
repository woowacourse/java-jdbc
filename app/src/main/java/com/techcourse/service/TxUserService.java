package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (final Connection connection = dataSource.getConnection()) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                userService.changePassword(id, newPassword, createBy);
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                rollback(connection, autoCommit, e);
                throw e;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection, final boolean autoCommit, final Throwable exception) {
        try {
            connection.rollback();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            exception.addSuppressed(e);
        }
    }
}
