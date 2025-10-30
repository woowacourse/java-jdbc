package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * 비즈니스 로직은 위임하고, 트랜잭션 경계 설정만 담당한다.
 */
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

    /**
     * DataSourceUtils를 통해 Connection을 획득하고 TransactionSynchronizationManager에 바인딩한다.
     */
    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        // DataSourceUtils를 통해 Connection 획득
        // 트랜잭션 동기화가 활성화되고, TransactionSynchronizationManager에 바인딩됨
        final var connection = DataSourceUtils.getConnection(dataSource);

        try {
            // 트랜잭션 시작
            connection.setAutoCommit(false);

            // 비즈니스 로직을 AppUserService에 위임
            // AppUserService는 트랜잭션을 신경쓰지 않고 비즈니스 로직만 처리
            userService.changePassword(id, newPassword, createdBy);

            // 트랜잭션 커밋
            connection.commit();
        } catch (Exception e) {
            // 트랜잭션 롤백
            // 로직 처리 중에 예외가 발생하면 원자성을 보장하기 위해 롤백한다.
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                throw new DataAccessException("Rollback failed", rollbackException);
            }
            throw new DataAccessException(e);
        } finally {
            // Connection 반환 및 트랜잭션 동기화 해제
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
