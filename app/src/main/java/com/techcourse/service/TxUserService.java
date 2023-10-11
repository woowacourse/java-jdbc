package com.techcourse.service;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplateException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return doService(() -> userService.findById(id));
    }

    private <T> T doService(final TransactionalDaoExecutor<T> executor) {
        try {
            return doDaoWithTransaction(executor);
        } catch (SQLException e) {
            throw new DataBaseAccessException(e.getMessage());
        }
    }

    private <T> T doDaoWithTransaction(final TransactionalDaoExecutor<T> executor) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            return executor.execute();
        } catch (JdbcTemplateException e) {
            connection.rollback();
            throw new DataBaseAccessException(e.getMessage());
        } finally {
            connection.setAutoCommit(true);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void insert(final User user) {
        doService(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        doService(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
