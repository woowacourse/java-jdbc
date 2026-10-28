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

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            beginTransaction(connection);
            try {
                userService.changePassword(id, newPassword, createdBy);
                commit(connection);
            } catch (RuntimeException e) {
                rollback(e, connection);
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void beginTransaction(Connection connection) {
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (SQLException sqlException) {
            throw new DataAccessException(sqlException);
        }
    }

    private static void rollback(final RuntimeException exception, final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException sqlException) {
            exception.addSuppressed(sqlException);
        }
        throw exception;
    }
}
