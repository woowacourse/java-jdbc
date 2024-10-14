package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public TxUserService(final AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public User findById(final long id) {
        return appUserService.findById(id);
    }

    @Override
    public void save(final User user) {
        appUserService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            appUserService.changePassword(id, newPassword, createBy);
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
            throw new DataAccessException("Failed to change password", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
