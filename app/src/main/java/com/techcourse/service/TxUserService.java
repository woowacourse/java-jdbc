package com.techcourse.service;

import com.techcourse.domain.User;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final DataSource dataSource) {
        this(dataSource, new AppUserService(new UserDao(dataSource), new UserHistoryDao(dataSource)));
    }

    TxUserService(final DataSource dataSource, final UserService userService) {
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
            throw new IllegalStateException("중첩 트랜잭션은 지원하지 않습니다.");
        }
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Throwable transactionFailure = null;
        try {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            Throwable failure = null;
            boolean completed = false;
            try {
                userService.changePassword(id, newPassword, createBy);
                connection.commit();
                completed = true;
            } catch (SQLException | RuntimeException | Error e) {
                failure = e;
                try {
                    connection.rollback();
                    completed = true;
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            } finally {
                try {
                    if (completed) {
                        connection.setAutoCommit(autoCommit);
                    }
                } catch (SQLException resetException) {
                    if (failure == null) {
                        log.warn("커밋 후 autoCommit 복원에 실패했습니다.", resetException);
                    } else {
                        failure.addSuppressed(resetException);
                    }
                }
            }
        } catch (SQLException e) {
            final var failure = new DataAccessException(e);
            transactionFailure = failure;
            throw failure;
        } catch (RuntimeException | Error e) {
            transactionFailure = e;
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException closeException) {
                if (transactionFailure == null) {
                    log.warn("트랜잭션 종료 후 연결 해제에 실패했습니다.", closeException);
                } else {
                    transactionFailure.addSuppressed(closeException);
                }
            }
        }
    }
}
