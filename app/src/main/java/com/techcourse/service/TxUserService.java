package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    public User findById(final long id) {
        return userService.findById(id);
    }

    public void insert(final User user) {
        userService.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (SQLException e) {
            rollbackConnection(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollbackConnection(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
