package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
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
        validateNoTransactionInProgress();
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        final boolean autoCommit = getAutoCommit(connection);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

            connection.commit();
        } catch (SQLException e) {
            rollback(connection, e);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(connection, e);
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            restoreAutoCommit(connection, autoCommit);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void validateNoTransactionInProgress() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("이미 진행 중인 트랜잭션이 있습니다.");
        }
    }

    private boolean getAutoCommit(final Connection connection) {
        try {
            return connection.getAutoCommit();
        } catch (SQLException e) {
            DataSourceUtils.releaseConnection(connection, dataSource);
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection, final Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }

    private void restoreAutoCommit(final Connection connection, final boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
