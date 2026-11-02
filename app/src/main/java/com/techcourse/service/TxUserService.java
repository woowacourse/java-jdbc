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
        final var conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

            conn.commit();
        } catch (SQLException e) {
            final var exception = new DataAccessException(e);
            rollback(conn, exception);
            throw exception;
        } catch (RuntimeException e) {
            rollback(conn, e);
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void rollback(final Connection conn, final RuntimeException cause) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            // 롤백까지 실패해도 처음 실패한 원인이 사라지지 않도록 함께 남긴다.
            cause.addSuppressed(e);
        }
    }
}
