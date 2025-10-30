package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TransactionUserService.class);
    private final DataSource dataSource;
    private final UserService userService;

    public TransactionUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    DataSourceUtils.releaseConnection(connection, dataSource);
                } catch (SQLException ignored) {
                    log.error("Connection release failed", ignored);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }
        }
    }
}
