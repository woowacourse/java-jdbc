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
        executeTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        executeTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }

    private void executeTransaction(Runnable action) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            action.run();
            connection.commit();
        } catch (SQLException e) {
            rollback(e, connection);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(e, connection);
            throw e;
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
