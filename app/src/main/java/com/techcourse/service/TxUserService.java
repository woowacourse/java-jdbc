package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public TxUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public User findByAccount(String account) {
        return userService.findByAccount(account);
    }

    @Override
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createBy);
            commit(connection);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        } catch (final DataAccessException e) {
            rollback(connection);
            throw e;
        }
    }

    private void commit(final Connection connection) {
        try {
            connection.commit();
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
