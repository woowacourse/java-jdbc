package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {

        void doInTransaction() throws Exception;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        execute(() -> {
            userService.changePassword(id, newPassword, createdBy);
        });
    }

    public <T> void execute(TransactionCallback<T> action) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            action.doInTransaction();
            connection.commit();
        } catch (Exception e) {
            rollbackTransaction(connection);
            throw new DataAccessException(e);
        } finally {
            closeConnection(connection);
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollbackTransaction(Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException rollbackException) {
            throw new DataAccessException(rollbackException);
        }
    }
}
