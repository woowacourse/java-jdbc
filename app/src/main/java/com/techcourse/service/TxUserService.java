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
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createBy);

            connection.commit();
        } catch (RuntimeException e) {
            rollback(connection, e);
            throw e;
        } catch (SQLException e) {
            final var exception = new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다. userId: " + id, e);
            rollback(connection, exception);
            throw exception;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }
    }

    private void rollback(final Connection connection, final RuntimeException cause) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }
}
