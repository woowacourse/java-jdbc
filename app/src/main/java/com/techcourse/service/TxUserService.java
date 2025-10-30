package com.techcourse.service;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(final long id) {
        return executeInTransaction(() -> userService.findById(id));
    }

    @Override
    public User findByAccount(final String account) {
        return executeInTransaction(() -> userService.findByAccount(account));
    }

    @Override
    public void save(final User user) {
        executeInTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeInTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    private void executeInTransaction(final Runnable runnable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);

            runnable.run();

            connection.commit();
        } catch (SQLException e) {
            handleRollback(connection);
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private <T> T executeInTransaction(final Supplier<T> supplier) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);

            T result = supplier.get();

            connection.commit();
            return result;
        } catch (SQLException e) {
            handleRollback(connection);
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void handleRollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to rollback");
        }
    }
}
