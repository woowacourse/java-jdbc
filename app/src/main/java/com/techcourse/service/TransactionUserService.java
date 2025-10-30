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

public class TransactionUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TransactionUserService.class);
    private final DataSource dataSource;
    private final UserService userService;

    public TransactionUserService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, conn);
            userService.changePassword(id, newPassword, createBy);
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
        } finally {
            try {
                TransactionSynchronizationManager.unbindResource(dataSource);
            } catch (IllegalStateException e) {
                log.warn("Failed to unbind resource from TransactionSynchronizationManager", e);
            } finally {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }
}
