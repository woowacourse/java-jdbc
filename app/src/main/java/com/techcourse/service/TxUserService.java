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

    private final DataSource dataSource;
    private final AppUserService userService;

    public TxUserService(AppUserService userService) {
        this.dataSource = DataSourceConfig.getInstance();
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            connection.setAutoCommit(false);
            try {
                userService.changePassword(id, newPassword, createdBy);
            } catch (Exception e) { // 비즈니스 로직 실패
                try {
                    connection.rollback();
                    throw new DataAccessException("Transaction rolled back due to an error", e);
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to rollback transaction", ex);
                }
            }
            connection.commit();
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
