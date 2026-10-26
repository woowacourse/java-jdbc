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
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            userService.save(user);
            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(e, connection);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new DataAccessException(e.getMessage());
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            close(connection);
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(e, connection);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new DataAccessException(e.getMessage());
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            close(connection);
        }
    }

    private void rollback(Exception e, Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            e.addSuppressed(ex);
        }
    }

    private void close(Connection connection) {
        try {
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
