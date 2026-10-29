package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException e) {
            final DataAccessException exception = new DataAccessException(e);
            rollback(connection, exception);
            throw exception;
        } catch (RuntimeException e) {
            rollback(connection, e);
            throw e;
        } finally {
            resetAutoCommit(connection);
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(final Connection connection, final RuntimeException cause) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }

    private void resetAutoCommit(final Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            log.warn("커넥션 반납 전 autoCommit 복구에 실패했습니다.", e);
        }
    }
}
