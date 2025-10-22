package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        executeInTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    private void executeInTransaction(Runnable runnable) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (RuntimeException e) {
            rollback(connection, e);
            throw e;
        } catch (SQLException e) {
            rollback(connection, e);
            throw new DataAccessException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Connection connection, Exception e) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            e.addSuppressed(ex);
        }
    }
}
