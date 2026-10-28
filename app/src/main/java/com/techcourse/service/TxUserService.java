package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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
        Connection connection = null;
        Throwable transactionFailure = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            final var exception = new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
            transactionFailure = exception;
            throw exception;
        } catch (CannotGetJdbcConnectionException e) {
            final var exception = new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e.getCause());
            transactionFailure = exception;
            throw exception;
        } catch (RuntimeException | Error e) {
            transactionFailure = e;
            throw e;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (CannotGetJdbcConnectionException e) {
                if (transactionFailure == null) {
                    throw new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e.getCause());
                }
                transactionFailure.addSuppressed(e.getCause());
            }
        }
    }
}
