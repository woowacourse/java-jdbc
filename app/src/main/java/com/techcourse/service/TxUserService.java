package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final AppUserService appUserService;

    public TxUserService(final AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public User findById(final Long id) {
        return appUserService.findById(id);
    }

    @Override
    public void save(final User user) {
        appUserService.save(user);
    }

    @Override
    public void changePassword(final Long userId, final String newPassword, final String createdBy) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            appUserService.changePassword(userId, newPassword, createdBy);
            conn.commit();
        } catch (final Exception e) {
            rollBack(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollBack(final Connection conn) {
        try {
            conn.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to rollback", e);
        }
    }
}
