package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, conn);
        try {
            changePasswordInTransaction(conn, id, newPassword, createdBy);
        } catch (RuntimeException | Error e) {
            release(conn, e);
            throw e;
        }
        release(conn);
    }

    private void changePasswordInTransaction(Connection conn, long id, String newPassword, String createdBy) {
        try {
            conn.setAutoCommit(false);
            changePasswordOrRollback(conn, id, newPassword, createdBy);
            conn.commit();
        } catch (SQLException e) {
            throw new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
        }
    }

    private void changePasswordOrRollback(Connection conn, long id, String newPassword, String createdBy) {
        try {
            userService.changePassword(id, newPassword, createdBy);
        } catch (RuntimeException | Error e) {
            rollback(conn, e);
            throw e;
        }
    }

    private void release(Connection conn) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(conn, dataSource);
    }

    private void release(Connection conn, Throwable cause) {
        try {
            release(conn);
        } catch (RuntimeException releaseException) {
            cause.addSuppressed(releaseException);
        }
    }

    private void rollback(Connection conn, Throwable cause) {
        try {
            conn.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }
}
