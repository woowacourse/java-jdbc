package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
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
        final var conn = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, conn);
        try {
            conn.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            conn.commit();
        } catch (SQLException e) {
            rollback(conn, e);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(conn, e);
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void rollback(final Connection conn, final Exception cause) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.error("Rollback failed. Original exception:", cause);
            final var rollbackException = new DataAccessException("Failed to rollback transaction", e);
            rollbackException.addSuppressed(cause);
            throw rollbackException;
        }
    }
}
