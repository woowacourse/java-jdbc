package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
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
        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
