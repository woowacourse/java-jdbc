package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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
    public User getById(final long id) {
        return userService.getById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }

                if (e instanceof DataAccessException) {
                    throw (DataAccessException) e;
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new DataAccessException("Transaction failed", e);
            }
        } catch (SQLException connectionEx) {
            throw new DataAccessException("Failed to set connection", connectionEx);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
