package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class TxUserService implements UserService {
    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this(userService, DataSourceConfig.getInstance());
    }

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = Objects.requireNonNull(userService);
        this.dataSource = Objects.requireNonNull(dataSource);
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
            throw new IllegalStateException("Nested transactions are not supported");
        }
        final var connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (SQLException | RuntimeException | Error ex) {
                rollback(connection, autoCommit, ex);
                throw ex;
            }
            connection.setAutoCommit(autoCommit);
        } catch (SQLException ex) {
            final var translated = new DataAccessException("Failed to change password transactionally", ex);
            failure = translated;
            throw translated;
        } catch (RuntimeException | Error ex) {
            failure = ex;
            throw ex;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException closeFailure) {
                if (failure == null) {
                    throw closeFailure;
                }
                failure.addSuppressed(closeFailure);
            }
        }
    }

    private void rollback(final Connection connection, final boolean autoCommit, final Throwable failure) {
        try {
            connection.rollback();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException rollbackFailure) {
            // Never enable auto-commit after a failed rollback: it could commit partial work.
            failure.addSuppressed(rollbackFailure);
        }
    }
}
