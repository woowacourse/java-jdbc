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
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (Throwable e) {
                rollbackAndRestoreAutoCommit(connection, e);
                throw e;
            }
            restoreAutoCommit(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    /**
     * 롤백에 성공한 경우에만 자동 커밋을 복원한다.
     * 롤백이 실패했는데 복원하면 {@code setAutoCommit(true)}가 미완료 트랜잭션을 커밋할 수 있으므로, 이때는 복원하지 않는다.
     */
    private void rollbackAndRestoreAutoCommit(final Connection connection, final Throwable cause) {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }

    /**
     * 커밋이 끝난 뒤의 복원 실패는 작업 결과를 바꾸지 않으므로 호출자에게 던지지 않고 로그만 남긴다.
     */
    private void restoreAutoCommit(final Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            log.warn("Failed to restore auto-commit after commit", e);
        }
    }
}
