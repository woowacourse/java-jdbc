package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(final long id) {
        return processTransaction(() -> userService.findById(id));
    }

    @Override
    public User findByAccount(final String account) {
        return processTransaction(() -> userService.findByAccount(account));
    }

    @Override
    public void save(final User user) {
        processTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        processTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }

    private <T> T processTransaction(Callable<T> transactionCallback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            final T result = transactionCallback.call();
            commit(connection);
            return result;
        } catch (final Exception e) {
            rollback(connection);
            log.warn("트랜잭션 실행 중 예외 발생", e);
            throw new DataAccessException(e);
        } finally {
            final Connection unboundConnection = TransactionSynchronizationManager.unbindResource(dataSource);
            try {
                unboundConnection.setAutoCommit(true);
                unboundConnection.close();
            } catch (final SQLException ex) {
                log.error("커넥션 종료 실패", ex);
            }
        }
    }

    private void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (final SQLException e) {
            log.error("commit 중 오류 발생", e);
            throw new DataAccessException(e);
        }
    }

    private void processTransaction(Runnable transactionCallback) {
        processTransaction(() -> {
            transactionCallback.run();
            return null;
        });
    }

    private void rollback(final Connection connection) {
        if (connection == null) {
            log.warn("rollback 실패: null connection");
            return;
        }
        try {
            connection.rollback();
            log.debug("rollback 완료");
        } catch (final SQLException e) {
            log.error("rollback 실패", e);
        }
    }
}
