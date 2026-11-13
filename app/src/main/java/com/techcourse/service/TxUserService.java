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

        boolean committed = false;
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
                committed = true;
            } catch (SQLException | RuntimeException | Error failure) {
                rollback(connection, autoCommit, failure);
                throw failure;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            if (committed) {
                log.warn("Failed to clean up connection after committing password change", e);
                return;
            }
            throw new DataAccessException("Failed to change password in a transaction", e);
        }
    }

    private void rollback(final Connection connection, final boolean autoCommit, final Throwable failure) {
        try {
            connection.rollback();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            failure.addSuppressed(e);
        }
    }
}
