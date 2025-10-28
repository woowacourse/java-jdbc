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

    public TxUserService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
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
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                userService.changePassword(id, newPassword, createdBy);
                conn.commit();
            } catch (Exception e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            if (conn != null) {
                rollback(conn, e);
            }
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException e) {
                    log.error("Failed to restore connection", e);
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.warn("Failed to close connection", e);
                }
            }
        }
    }

    private void rollback(Connection conn, Exception originalException) {
        try {
            conn.rollback();
        } catch (SQLException rollbackEx) {
            log.error("Rollback failed", rollbackEx);
            originalException.addSuppressed(rollbackEx);
        }
    }
}
