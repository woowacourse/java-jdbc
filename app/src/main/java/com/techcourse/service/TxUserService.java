package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    public User findById(final long id) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            connection.setReadOnly(true);

            User user = userService.findById(id);

            connection.commit();
            return user;
        } catch (final SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void insert(final User user) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            userService.insert(user);

            connection.commit();
        } catch (RuntimeException | SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {

        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createBy);

            connection.commit();
        } catch (RuntimeException | SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollback(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

}
