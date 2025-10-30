package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
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

    public TxUserService(final UserService userService) {
        this.dataSource = DataSourceConfig.getInstance();
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
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, conn);
            conn.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

            conn.commit();
        } catch (final Exception e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            closeConnection(conn);
        }
    }

    private void closeConnection(final Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (final SQLException ex) {
                log.error("Connection close failed", ex);
            }
        }
    }

    private void rollback(final Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (final SQLException ex) {
                log.error("Rollback failed", ex);
            }
        }
    }
}
