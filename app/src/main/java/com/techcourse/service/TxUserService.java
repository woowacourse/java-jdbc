package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.exception.ConnectionException;
import org.springframework.jdbc.exception.RollbackFailException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        executeWithTransaction(connection -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        executeWithTransaction(connection -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }

    private <T> T executeWithTransaction(TransactionExecutor<T> transactionExecutor) {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.setActualTransactionActive(true);
            T result = transactionExecutor.execute(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        } catch (DataAccessException e) {
            rollback(connection);
            throw e;
        } finally {
            close(connection);
        }
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(DataSourceConfig.getInstance());
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RollbackFailException(e.getMessage());
        }
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
                TransactionSynchronizationManager.setActualTransactionActive(false);
                TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
            } catch (SQLException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }
}
