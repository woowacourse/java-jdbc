package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            userServiceInterface.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Rollback failed", ex);
                }
            }
            throw new DataAccessException(e);
        } finally {
            if (connection != null) {
                try {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                    Connection unboundResource = TransactionSynchronizationManager.unbindResource(dataSource);
                    log.info("Resource has been unbound: {}", unboundResource);
                } catch (Exception ex) {
                    log.error("Failed to release connection or unbind resource", ex);
                }
            }
        }
    }

    @Override
    public void save(User user) {
        userServiceInterface.save(user);
    }

    @Override
    public User findById(long id) {
        return userServiceInterface.findById(id);
    }
}

