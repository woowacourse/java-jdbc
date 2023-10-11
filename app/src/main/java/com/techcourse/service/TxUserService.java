package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (final Exception e) {
            rollbackAndThrow(connection, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollbackAndThrow(final Connection connection, final Exception businessException) {
        try {
            connection.rollback();
            throw new DataAccessException(businessException);
        } catch (final SQLException rollbackException) {
            throw new DataAccessException(rollbackException);
        }
    }
}
