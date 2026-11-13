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
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            executeTransaction(connection, id, newPassword, createdBy);
        } catch (SQLException e) {
            DataSourceUtils.releaseConnection(connection, dataSource, e);
            throw new DataAccessException(e);
        } catch (RuntimeException | Error failure) {
            DataSourceUtils.releaseConnection(connection, dataSource, failure);
            throw failure;
        }
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    private void executeTransaction(final Connection connection, final long id,
                                    final String newPassword, final String createdBy) throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (SQLException | RuntimeException | Error failure) {
                rollback(connection, autoCommit, failure);
                throw failure;
            }
            connection.setAutoCommit(autoCommit);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollback(final Connection connection, final boolean autoCommit, final Throwable failure) {
        try {
            connection.rollback();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }
}
