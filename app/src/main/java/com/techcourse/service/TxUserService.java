package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
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
    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createBy);

            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("Rollback failed", rollbackEx);
                }
            }
            throw new DataAccessException(e);
        } finally {
            if (connection != null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }
    }
}
