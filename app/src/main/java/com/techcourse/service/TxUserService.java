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
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeInTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }

    private void executeInTransaction(final Runnable runnable) {
        final boolean hadExistingConnection = DataSourceUtils.hasResource(dataSource);

        if (hadExistingConnection) {
            runnable.run();
            return;
        }

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (final Exception e) {
            rollback(connection, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollback(final Connection connection, final Exception originalException) {
        try {
            connection.rollback();
        } catch (final SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
        throw new DataAccessException("트랜잭션 실행 실패", originalException);
    }
}
