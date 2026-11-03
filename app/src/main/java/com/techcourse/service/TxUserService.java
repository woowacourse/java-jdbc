package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

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
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("A transaction is already active for this DataSource");
        }
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable failure = null;
        try {
            changePasswordInTransaction(connection, id, newPassword, createdBy);
        } catch (SQLException e) {
            failure = e;
            throw new DataAccessException("Failed to change password. userId: " + id, e);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            releaseConnection(connection, failure);
        }
    }

    private void changePasswordInTransaction(final Connection connection, final long id,
                                             final String newPassword, final String createdBy) throws SQLException {
        final var autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException | RuntimeException | Error e) {
            rollbackAndRestore(connection, autoCommit, e);
            throw e;
        }
        restoreAutoCommit(connection, autoCommit);
    }

    private void rollbackAndRestore(final Connection connection, final boolean autoCommit, final Throwable failure) {
        try {
            connection.rollback();
            // 롤백이 실패한 커넥션에서 autoCommit을 켜면 미완료 변경이 커밋될 수 있다.
            restoreAutoCommit(connection, autoCommit);
        } catch (SQLException | RuntimeException e) {
            failure.addSuppressed(e);
        }
    }

    private void restoreAutoCommit(final Connection connection, final boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to restore auto-commit", e);
        }
    }

    private void releaseConnection(final Connection connection, final Throwable failure) {
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
