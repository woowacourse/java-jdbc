package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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
    public void changePassword(final long id, final String newPassword, final String createBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("TxUserService requires an independent transaction");
        }
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            boolean transactionCompleted = false;
            Throwable failure = null;
            try {
                userService.changePassword(id, newPassword, createBy);
                connection.commit();
                transactionCompleted = true;
            } catch (SQLException | RuntimeException | Error e) {
                failure = e;
                transactionCompleted = rollback(connection, e);
                throw e;
            } finally {
                // 롤백 실패 시 자동 커밋을 켜면 미완료 변경이 커밋될 수 있다.
                try {
                    if (transactionCompleted) {
                        restoreAutoCommit(connection, autoCommit, failure);
                    }
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DataAccessException("Failed to change password in transaction", e.getCause());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to change password in transaction", e);
        }
    }

    private boolean rollback(final Connection connection, final Throwable failure) {
        try {
            connection.rollback();
            return true;
        } catch (SQLException e) {
            failure.addSuppressed(e);
            return false;
        }
    }

    private void restoreAutoCommit(final Connection connection, final boolean autoCommit,
                                   final Throwable failure) throws SQLException {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            if (failure == null) {
                throw e;
            }
            failure.addSuppressed(e);
        }
    }
}
