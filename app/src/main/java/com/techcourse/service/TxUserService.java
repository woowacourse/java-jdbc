package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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
        final var connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            changePasswordInTransaction(connection, id, newPassword, createdBy);
        } catch (RuntimeException | Error failure) {
            try {
                releaseConnection(connection);
            } catch (RuntimeException | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
        releaseConnection(connection);
    }

    private void changePasswordInTransaction(final Connection connection, final long id,
                                              final String newPassword, final String createdBy) {
        try {
            final var originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (SQLException | RuntimeException | Error failure) {
                try {
                    // 롤백 실패 상태에서 autoCommit을 true로 바꾸면 남은 변경이 커밋될 수 있다.
                    // 따라서 롤백 성공 후에만 원래 설정을 복구한다.
                    connection.rollback();
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
                throw failure;
            }

            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to change password", e);
        }
    }

    private void releaseConnection(final Connection connection) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
