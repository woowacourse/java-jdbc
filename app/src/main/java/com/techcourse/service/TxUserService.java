package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(
            final DataSource dataSource,
            final UserService userService
    ) {
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
    public void changePassword(
            final long id,
            final String newPassword,
            final String createdBy
    ) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (final Exception e) {
                connection.rollback();
                throw new DataAccessException(e);
            }
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
