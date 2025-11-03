package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        final boolean isNewTransaction = TransactionSynchronizationManager.getResource(dataSource) == null;
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            return userService.findById(id);
        } finally {
            if (isNewTransaction && connection != null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    @Override
    public void save(final User user) {
        final boolean isNewTransaction = TransactionSynchronizationManager.getResource(dataSource) == null;
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            if (isNewTransaction) {
                connection.setAutoCommit(false);
            }

            userService.save(user);

            if (isNewTransaction) {
                connection.commit();
            }
        } catch (Exception e) {
            if (isNewTransaction && connection != null) {
                rollbackTransaction(connection);
            }
            throw new DataAccessException(e);
        } finally {
            if (isNewTransaction && connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        final boolean isNewTransaction = TransactionSynchronizationManager.getResource(dataSource) == null;
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            if (isNewTransaction) {
                connection.setAutoCommit(false);
            }

            userService.changePassword(id, newPassword, createdBy);

            if (isNewTransaction) {
                connection.commit();
            }
        } catch (Exception e) {
            if (isNewTransaction && connection != null) {
                rollbackTransaction(connection);
            }
            throw new DataAccessException(e);
        } finally {
            if (isNewTransaction && connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    private void rollbackTransaction(final Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }
}
