package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final Logger logger = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        dataSource = DataSourceConfig.getInstance();
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        boolean isNewConnection = DataSourceUtils.isNewConnection(dataSource);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            if (isNewConnection) {
                connection.setReadOnly(true);
            }
            return userService.findById(id);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            if (isNewConnection) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    @Override
    public void save(final User user) {
        boolean isNewConnection = DataSourceUtils.isNewConnection(dataSource);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            if (isNewConnection) {
                connection.setAutoCommit(false);
            }
            userService.save(user);
            if (isNewConnection) {
                connection.commit();
            }
        } catch (Exception e) {
            try {
                if (isNewConnection) {
                    connection.rollback();
                }
            } catch (SQLException sqlException) {
                logger.error("롤백 실패", sqlException);
            }
            throw new DataAccessException(e);
        } finally {
            if (isNewConnection) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        boolean isNewConnection = DataSourceUtils.isNewConnection(dataSource);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            if (isNewConnection) {
                connection.setAutoCommit(false);
            }
            userService.changePassword(id, newPassword, createBy);
            if (isNewConnection) {
                connection.commit();
            }
        } catch (Exception e) {
            try {
                if (isNewConnection) {
                    connection.rollback();
                }
            } catch (SQLException sqlException) {
                logger.error("롤백 실패", sqlException);
            }
            throw new DataAccessException(e);
        } finally {
            if (isNewConnection) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }        }
    }
}
