package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TrxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TrxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return executeTransactionTemplate(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        executeTransactionTemplate(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeTransactionTemplate(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }

    public <T> T executeTransactionTemplate(Supplier<T> supplier) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            T result = supplier.get();
            DataSourceUtils.commit(connection);
            return result;
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
