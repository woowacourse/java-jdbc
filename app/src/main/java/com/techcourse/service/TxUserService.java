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

    private static final DataSource dataSource = DataSourceConfig.getInstance();
    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                userService.changePassword(id, newPassword, createdBy);
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
            connection.commit();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        } finally {
            Connection bound = null;
            if (TransactionSynchronizationManager.hasResource(dataSource)) {
                bound = TransactionSynchronizationManager.unbindResource(dataSource);
            }
            DataSourceUtils.releaseConnection(bound != null ? bound : connection, dataSource);
        }
    }
}
