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
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            TransactionSynchronizationManager.bindResource(dataSource, conn);
            conn.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

            conn.commit();
        } catch (Exception e) {
            handleException(conn, e);
        } finally {
            cleanUpConnection(conn);
        }
    }

    private void handleException(final Connection conn, final Exception exception) {
        log.error(exception.getMessage(), exception);

        if (exception instanceof SQLException) {
            rollback(conn, exception);
            throw new DataAccessException(exception.getMessage(), exception);
        }

        if (exception instanceof RuntimeException runtimeException) {
            rollback(conn, exception);
            throw runtimeException;
        }

        commit(conn, exception);
        throw new RuntimeException(exception.getMessage(), exception);
    }

    private void rollback(Connection conn, Throwable originalException) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException rollbackEx) {
            log.error("Rollback failed", rollbackEx);
            originalException.addSuppressed(rollbackEx);
        }
    }

    private void commit(Connection conn, Throwable originalException) {
        if (conn == null) {
            return;
        }
        try {
            conn.commit();
        } catch (SQLException commitEx) {
            log.error("Commit failed", commitEx);
            originalException.addSuppressed(commitEx);
        }
    }

    private void cleanUpConnection(final Connection conn) {
        if (conn == null) {
            return;
        }

        TransactionSynchronizationManager.unbindResource(dataSource);
        restoreAutoCommit(conn);
        closeConnection(conn);
    }

    private void restoreAutoCommit(final Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void closeConnection(final Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
