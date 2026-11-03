package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this(userService, DataSourceConfig.getInstance());
    }

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return executeInReadOnlyTransaction(() -> userService.findById(id));
    }

    @Override
    public void save(final User user) {
        executeInTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeInTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }

    private void executeInTransaction(final Runnable action) {
        execute(false, () -> {
            action.run();
            return null;
        });
    }

    private <T> T executeInReadOnlyTransaction(final Supplier<T> action) {
        return execute(true, action);
    }

    private <T> T execute(final boolean readOnly, final Supplier<T> action) {
        final Connection conn = getConnection();
        TransactionSynchronizationManager.bindResource(dataSource, conn);
        try {
            conn.setReadOnly(readOnly);
            conn.setAutoCommit(false);
            final T result = action.get();
            conn.commit();
            return result;
        } catch (RuntimeException e) {
            rollback(conn, e);
            throw e;
        } catch (SQLException e) {
            rollback(conn, e);
            throw new DataAccessException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            reset(conn);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection conn, final Exception cause) {
        try {
            conn.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }

    private void reset(final Connection conn) {
        try {
            conn.setAutoCommit(true);
            conn.setReadOnly(false);
        } catch (SQLException e) {
            log.warn("커넥션 상태 복구 실패", e);
        }
    }
}