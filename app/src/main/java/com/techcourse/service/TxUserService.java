package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.TransactionFailedException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService appUserService;
    private final DataSource dataSource;

    public TxUserService(UserService userService) {
        this.appUserService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(long id) {
        return appUserService.findById(id);
    }

    @Override
    public void save(User user) {
        appUserService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        try {
            changePasswordWithTransaction(id, newPassword, createdBy);
        } catch (SQLException e) {
            throw new IllegalStateException("패스워드 변경 실패: 데이터베이스 연결 오류가 발생했습니다.", e);
        }
    }

    private void changePasswordWithTransaction(final long id, final String newPassword, final String createBy) throws SQLException {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        conn.setAutoCommit(false);

        try {
            appUserService.changePassword(id, newPassword, createBy);
            conn.commit();
        } catch (DataAccessException e) {
            handleRollback(conn);
            throw new TransactionFailedException("트랜잭션 실패: 패스워드 변경 중 오류가 발생했습니다.", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void handleRollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new TransactionFailedException("트랜잭션 실패: 롤백 중 오류가 발생했습니다.", e);
        }
    }
}

