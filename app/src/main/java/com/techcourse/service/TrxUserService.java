package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TrxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TrxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            User user = userService.findById(id);
            DataSourceUtils.commit(connection);
            return user;
        } catch (SQLException e) {
            DataSourceUtils.rollback(connection);
            throw new CannotGetJdbcConnectionException("Cannot set autocommit");
        } catch (Exception e) {
            DataSourceUtils.rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void insert(final User user) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            userService.insert(user);
            DataSourceUtils.commit(connection);
        } catch (SQLException e) {
            DataSourceUtils.rollback(connection);
            throw new CannotGetJdbcConnectionException("Cannot set autocommit");
        } catch (Exception e) {
            DataSourceUtils.rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createBy);
            DataSourceUtils.commit(connection);
        } catch (SQLException e) {
            DataSourceUtils.rollback(connection);
            throw new CannotGetJdbcConnectionException("Cannot set autocommit");
        } catch (Exception e) {
            DataSourceUtils.rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
