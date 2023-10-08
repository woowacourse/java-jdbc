package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;

public class TxUserService implements UserService {

    private UserService userService;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            User user = userService.findById(id);
            connection.commit();
            return user;
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLTransactionRollbackException(e.getMessage());
        }
    }

    @Override
    public void insert(User user) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            userService.insert(user);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLTransactionRollbackException(e.getMessage());
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            User user = findById(id);
            user.changePassword(newPassword);
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLTransactionRollbackException(e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
