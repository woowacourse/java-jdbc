package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.dataSource = DataSourceConfig.getInstance();
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return userService.findById(id);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void save(final User user) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            userService.save(user);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DataAccessException("some exception occurred while transaction", e);
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("sql exception occurred", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
