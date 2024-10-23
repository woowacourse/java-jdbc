package com.techcourse.service;

import java.sql.SQLException;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

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
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        final var dataSource = DataSourceConfig.getInstance();
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex.getMessage(), ex);
            }
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
