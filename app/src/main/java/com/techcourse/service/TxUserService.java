package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = Objects.requireNonNull(userService, "userService must not be null");
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
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
            throw new IllegalStateException("A transaction is already bound to this DataSource");
        }

        var committed = false;
        try {
            final var connection = DataSourceUtils.doGetConnection(dataSource);
            Throwable failure = null;
            try {
                final var autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                TransactionSynchronizationManager.bindResource(dataSource, connection);
                try {
                    executeAndCommit(connection, autoCommit, id, newPassword, createdBy);
                    committed = true;
                    connection.setAutoCommit(autoCommit);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            } catch (SQLException | RuntimeException | Error e) {
                failure = e;
                throw e;
            } finally {
                DataSourceUtils.doReleaseConnection(connection, dataSource, failure);
            }
        } catch (SQLException e) {
            if (committed) {
                throw new TransactionCleanupException(e);
            }
            throw new DataAccessException("Failed to change password in a transaction", e);
        }
    }

    private void executeAndCommit(final Connection connection, final boolean autoCommit, final long id,
                                  final String newPassword, final String createdBy) throws SQLException {
        try {
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException | RuntimeException | Error e) {
            rollbackAndRestore(connection, autoCommit, e);
            throw e;
        }
    }

    private void rollbackAndRestore(final Connection connection, final boolean autoCommit, final Throwable cause) {
        try {
            connection.rollback();
            // 롤백 성공 후에만 복원한다. 롤백 실패 시 자동 커밋을 켜면 암묵적으로 커밋할 수 있다.
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }

}
