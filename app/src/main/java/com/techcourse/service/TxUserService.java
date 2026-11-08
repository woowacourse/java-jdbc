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
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        validateNoTransactionInProgress();
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            executeTransaction(connection, id, newPassword, createdBy);
        } catch (SQLException e) {
            failure = e;
            throw new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            releaseConnection(connection, failure);
        }
    }

    private void validateNoTransactionInProgress() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("해당 DataSource에 이미 트랜잭션 연결이 등록되어 있습니다.");
        }
    }

    private void executeTransaction(final Connection connection, final long id,
                                    final String newPassword, final String createdBy) throws SQLException {
        final boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException | RuntimeException | Error e) {
            if (rollback(connection, e)) {
                restoreAutoCommit(connection, previousAutoCommit, e);
            }
            throw e;
        }
        restoreAutoCommit(connection, previousAutoCommit, null);
    }

    private boolean rollback(final Connection connection, final Throwable failure) {
        try {
            connection.rollback();
            return true;
        } catch (SQLException | RuntimeException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
            return false;
        }
    }

    private void restoreAutoCommit(final Connection connection, final boolean previousAutoCommit,
                                   final Throwable failure) {
        try {
            connection.setAutoCommit(previousAutoCommit);
        } catch (SQLException | RuntimeException restoreFailure) {
            if (failure == null) {
                throw new DataAccessException("트랜잭션 연결 상태 복원에 실패했습니다.", restoreFailure);
            }
            failure.addSuppressed(restoreFailure);
        }
    }

    private void releaseConnection(final Connection connection, final Throwable failure) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (RuntimeException closeFailure) {
            if (failure == null) {
                throw new DataAccessException("트랜잭션 연결 반환에 실패했습니다.", closeFailure);
            }
            failure.addSuppressed(closeFailure);
        }
    }
}
