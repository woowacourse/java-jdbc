package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {
    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            return userService.findById(id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }

    @Override
    public void insert(final User user) {
        final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            userService.insert(user);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (final Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }
}
