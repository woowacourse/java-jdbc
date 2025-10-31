package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.TransactionCallback;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);
    private final UserServiceInterface userServiceInterface;

    public TxUserService(UserServiceInterface userServiceInterface) {
        this.userServiceInterface = userServiceInterface;
    }

    // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.
    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeTransaction(() -> userServiceInterface.changePassword(id, newPassword, createdBy));
    }

    @Override
    public void save(User user) {
        executeTransaction(() -> userServiceInterface.save(user));
    }

    @Override
    public User findById(long id) {
        return executeTransaction(() -> userServiceInterface.findById(id));
    }

    private void executeTransaction(Runnable runnable) {
        executeTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    private <T> T executeTransaction(TransactionCallback<T> transactionCallback) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            T result = transactionCallback.doInTransaction();
            connection.commit();
            return result;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Rollback failed", ex);
                    throw new DataAccessException("Rollback failed");
                }
            }
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Rollback failed", ex);
                    throw new DataAccessException("Rollback failed");
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                } catch (Exception ex) {
                    log.error("Failed to release connection or unbind resource", ex);
                }
            }
        }
    }
}

