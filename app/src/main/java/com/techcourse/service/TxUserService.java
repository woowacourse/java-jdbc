package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(long id) {
        return processInTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        processInTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        processInTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    private void processInTransaction(Runnable runnable) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private <T> T processInTransaction(Supplier<T> supplier) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            T result = supplier.get();
            connection.commit();
            return result;
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }
}
