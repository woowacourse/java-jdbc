package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

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
        final var dataSource = DataSourceConfig.getInstance();
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            userService.insert(user);

            connection.commit();
        } catch (final Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var dataSource = DataSourceConfig.getInstance();
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createBy);

            connection.commit();
        } catch (final Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
