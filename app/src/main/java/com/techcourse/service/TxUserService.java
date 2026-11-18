package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final UserService userService) {
        this(DataSourceConfig.getInstance(), userService);
    }

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
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
    public void changePassword(final long id, final String newPassword, final String createBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("TxUserService requires an independent transaction");
        }
        Connection connection = null;
        Throwable failure = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            executeTransaction(connection, id, newPassword, createBy);
        } catch (SQLException | CannotGetJdbcConnectionException e) {
            final var translated = new DataAccessException("Failed to change password in transaction",
                    e instanceof CannotGetJdbcConnectionException ? e.getCause() : e);
            failure = translated;
            throw translated;
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            releaseConnection(connection, failure);
        }
    }

    private void executeTransaction(final Connection connection, final long id,
                                    final String newPassword, final String createdBy) throws SQLException {
        final boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException | RuntimeException | Error e) {
            rollbackAndRestore(connection, autoCommit, e);
            throw e;
        }
        // 커밋 이후 복구 실패는 이미 완료된 트랜잭션의 롤백으로 이어지지 않는다.
        connection.setAutoCommit(autoCommit);
    }

    private void rollbackAndRestore(final Connection connection, final boolean autoCommit,
                                    final Throwable failure) {
        try {
            connection.rollback();
            // 롤백에 성공한 경우에만 복구한다. 실패한 연결의 미완료 변경을 커밋하지 않는다.
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            failure.addSuppressed(e);
        }
    }

    private void releaseConnection(final Connection connection, final Throwable failure) {
        if (connection == null) {
            return;
        }
        if (TransactionSynchronizationManager.getResource(dataSource) == connection) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            if (failure != null) {
                failure.addSuppressed(e.getCause());
            } else {
                throw new DataAccessException("Failed to release transaction Connection", e.getCause());
            }
        }
    }
}
