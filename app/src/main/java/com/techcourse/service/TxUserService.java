package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this(userService, DataSourceConfig.getInstance());
    }

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
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
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable originalException = null;
        try {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                userService.changePassword(id, newPassword, createBy);
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException cleanupException) {
                    e.addSuppressed(cleanupException);
                }
                throw e;
            }

            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            final var exception = new DataAccessException(e);
            originalException = exception;
            throw exception;
        } catch (RuntimeException | Error e) {
            originalException = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource, originalException);
        }
    }
}
