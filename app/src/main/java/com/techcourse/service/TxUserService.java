package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

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
    public User findById(long id) {
        return executeTransaction(connection -> {
            userService.findById(id);
            return null;
        });
    }

    @Override
    public void save(User user) {
        executeTransaction(connection -> {
            userService.save(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        executeTransaction(connection -> {
            userService.changePassword(id, newPassword, createdBy);
            return null;
        });
    }

    private <R> R executeTransaction(Function<Connection, R> function) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            R result = function.apply(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}
