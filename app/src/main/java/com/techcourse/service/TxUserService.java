package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

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
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        final Connection connection = begin();
        try {
            userService.changePassword(id, newPassword, createdBy);
            commit(connection);
        } catch (RuntimeException | Error failure) {
            rollback(connection, failure);
            throw failure;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    /** 자동 커밋을 끈 연결만 바인딩해, 시작에 실패하면 스레드에 연결을 남기지 않는다. */
    private Connection begin() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            DataSourceUtils.releaseConnection(connection, dataSource);
            throw new DataAccessException("트랜잭션을 시작하지 못했습니다.", e);
        }
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        return connection;
    }

    private void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("트랜잭션을 커밋하지 못했습니다.", e);
        }
    }

    private void rollback(final Connection connection, final Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }
}
