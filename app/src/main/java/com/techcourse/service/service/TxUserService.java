package com.techcourse.service.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
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
        /* ===== 트랜잭션 영역 ===== */
        Connection connection = null;

        try {
            // 1. Connection 생성
            connection = dataSource.getConnection();

            // 2. 트랜잭션 시작 (autoCommit 비활성화)
            connection.setAutoCommit(false);

            // 3. ThreadLocal에 Connection 바인딩
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            // 4. 비즈니스 로직 실행
            userService.changePassword(id, newPassword, createBy);

            // 5. 커밋
            connection.commit();
        } catch (SQLException e) {
            rollbackSafely(connection);
            throw new DataAccessException("트랜잭션 처리 중 SQL 오류가 발생했습니다.", e);
        } catch (Exception e) {
            rollbackSafely(connection);
            throw new DataAccessException("트랜잭션 실행 중 문제가 발생했습니다.", e);
        } finally {
            // 6. ThreadLocal에서 제거
            TransactionSynchronizationManager.unbindResource(dataSource);

            // 7. Connection 닫기
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new DataAccessException("커넥션 닫는 중 문제가 발생했습니다.");
                }
            }
        }
    }

    private void rollbackSafely(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                // 로그 처리
            }
        }
    }
}
