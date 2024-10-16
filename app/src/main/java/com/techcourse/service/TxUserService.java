package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService service;

    public TxUserService(final UserService userService) {
        this.service = userService;
    }

    @Override
    public User findById(long id) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
            connection.setAutoCommit(false);
            User user = service.findById(id);
            connection.commit();
            return user;
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public void insert(User user) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
            connection.setAutoCommit(false);
            service.insert(user);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
            connection.setAutoCommit(false);
            service.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            closeConnection(connection);
        }
    }

    private static void rollback(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException rollbackE) {
            throw new DataAccessException(rollbackE);
        }
    }

    private static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
            connection.close();
        } catch (SQLException closeE) {
            throw new DataAccessException(closeE);
        }
    }
}
