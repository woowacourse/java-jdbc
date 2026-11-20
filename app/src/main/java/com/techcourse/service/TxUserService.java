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

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

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
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            userService.changePassword(id, newPassword, createdBy);
            return;
        }

        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean autoCommit = true;
        boolean started = false;
        boolean completed = false;
        Throwable failure = null;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
            completed = true;
        } catch (SQLException e) {
            DataAccessException transactionFailure =
                    new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
            failure = transactionFailure;
            completed = rollback(connection, started, transactionFailure);
            throw transactionFailure;
        } catch (RuntimeException | Error e) {
            failure = e;
            completed = rollback(connection, started, e);
            throw e;
        } finally {
            if (TransactionSynchronizationManager.getResource(dataSource) == connection) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
            cleanup(connection, autoCommit, completed, failure);
        }
    }

    private boolean rollback(final Connection connection, final boolean started, final Throwable failure) {
        if (!started) {
            return false;
        }
        try {
            connection.rollback();
            return true;
        } catch (SQLException e) {
            failure.addSuppressed(e);
            return false;
        }
    }

    private void cleanup(final Connection connection, final boolean autoCommit,
                         final boolean completed, final Throwable failure) {
        if (completed) {
            try {
                connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                handleCleanupFailure(e, failure);
            }
        }
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (RuntimeException e) {
            handleCleanupFailure(e, failure);
        }
    }

    private void handleCleanupFailure(final Exception cleanupFailure, final Throwable failure) {
        if (failure != null) {
            failure.addSuppressed(cleanupFailure);
        } else {
            log.warn("커밋 완료 후 연결 정리에 실패했습니다.", cleanupFailure);
        }
    }
}
