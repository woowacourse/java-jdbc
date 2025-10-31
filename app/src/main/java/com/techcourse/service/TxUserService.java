package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection existingConnection = TransactionSynchronizationManager.getResource(dataSource);
        boolean isNewTransaction = (existingConnection == null);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        if (isNewTransaction) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
        }

        try {
            if (isNewTransaction) {
                connection.setAutoCommit(false);
            }

            userService.changePassword(id, newPassword, createdBy);

            if (isNewTransaction) {
                connection.commit();
            }
        } catch (RuntimeException e) {
            if (isNewTransaction) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("Failed to rollback transaction", rollbackEx);
                }
            }
            throw e;
        } catch (Exception e) {
            if (isNewTransaction) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("Failed to rollback transaction", rollbackEx);
                }
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            if (isNewTransaction) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }
}
