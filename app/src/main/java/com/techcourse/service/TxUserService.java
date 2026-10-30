package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {
    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

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
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            beginTransaction(connection);
            try {
                userService.changePassword(id, newPassword, createdBy);
                commit(connection);
            } catch (RuntimeException e) {
                rollback(e, connection);
            } finally {
                endTransaction(connection);
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static void endTransaction(final Connection connection) throws SQLException {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            log.warn("Failed to reset autocommit", e);
        }
    }

    private void beginTransaction(final Connection connection) {
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
