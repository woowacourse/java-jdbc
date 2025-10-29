package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);
    private static final DataSource dataSource = DataSourceConfig.getInstance();

    private final AppUserService appUserService;

    public TxUserService(final AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public User findById(final long id) {
        return appUserService.findById(id);
    }

    public void insert(final User user) {
        appUserService.insert(user);
    }

    public void save(final User user) {
        appUserService.save(user);
    }

    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            try {
                appUserService.changePassword(id, newPassword, createdBy);
            } catch (Exception e) {
                rollback(e, connection);
                throw e;
            }
            connection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Exception appException, Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            log.error(rollbackEx.getMessage(), rollbackEx);
            appException.addSuppressed(rollbackEx);
        }
    }
}
