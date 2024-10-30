package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return executeWithTransaction(userService -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        executeWithTransactionVoid(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeWithTransactionVoid(() -> userService.changePassword(id, newPassword, createdBy));
    }

    private <T> T executeWithTransaction(Function<UserService, T> action) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            T t = action.apply(userService);

            connection.commit();
            return t;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Failed to rollback transaction", rollbackEx);
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void executeWithTransactionVoid(Runnable action) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            action.run();

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Failed to rollback transaction", rollbackEx);
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
