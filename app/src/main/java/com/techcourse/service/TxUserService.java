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
        this(DataSourceConfig.getInstance(), userService);
    }

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        DataAccessException failure = null;

        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(connection, e);
            failure = new DataAccessException(e);
            throw failure;
        } finally {
            release(connection, failure);
        }
    }

    private void rollback(Connection connection, Exception error) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            error.addSuppressed(e);
        }
    }

    private void release(Connection connection, DataAccessException failure) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        try {
            try {
                connection.setAutoCommit(true);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            if (failure != null) {
                failure.addSuppressed(e);
                return;
            }
            throw new DataAccessException(e);
        }
    }
}
