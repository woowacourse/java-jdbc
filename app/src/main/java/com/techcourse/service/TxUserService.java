package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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
        try {
            executeTransaction(connection, id, newPassword, createdBy);
        } catch (SQLException e) {
            throw new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
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
                restoreAutoCommit(connection, previousAutoCommit);
            }
            throw e;
        }
        restoreAutoCommit(connection, previousAutoCommit);
    }

    private boolean rollback(final Connection connection, final Throwable originalException) {
        try {
            connection.rollback();
            return true;
        } catch (SQLException | RuntimeException rollbackException) {
            originalException.addSuppressed(rollbackException);
            return false;
        }
    }

    private void restoreAutoCommit(final Connection connection, final boolean previousAutoCommit) {
        try {
            connection.setAutoCommit(previousAutoCommit);
        } catch (SQLException | RuntimeException e) {
            log.warn("트랜잭션 연결 상태 복원에 실패했습니다.", e);
        }
    }
}
