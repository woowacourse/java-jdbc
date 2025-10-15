package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        dataSource = DataSourceConfig.getInstance();
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
        inTransaction(connection -> userService.changePassword(id, newPassword, createdBy));
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws Exception;
    }

    private void inTransaction(SqlConsumer<Connection> work) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            System.out.println(
                    "[DEBUG] TxUserService connection=" + connection.hashCode() + ", closed=" + connection.isClosed());
            try {
                work.accept(connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throwUncheckedException(ex);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    private void throwUncheckedException(Exception ex) {
        if (ex instanceof RuntimeException re) {
            throw re;
        }
        throw new RuntimeException(ex);
    }
}
