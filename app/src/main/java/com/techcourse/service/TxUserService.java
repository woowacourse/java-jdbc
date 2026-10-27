package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.SQLException;

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
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("이미 진행 중인 트랜잭션이 있습니다.");
        }

        try (var connection = DataSourceUtils.getConnection(dataSource);) {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            Throwable failure = null;
            boolean rollbackFailed = false;
            try {
                userService.changePassword(id, newPassword, createBy);
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                failure = e;
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                    rollbackFailed = true;
                }
                throw e;
            } finally {
                if (!rollbackFailed) {
                    try {
                        connection.setAutoCommit(autoCommit);
                    } catch (SQLException resetException) {
                        if (failure == null) {
                            throw resetException;
                        }
                        failure.addSuppressed(resetException);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
