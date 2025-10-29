package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        // DataSourceUtils를 통해 Connection 획득
        // 트랜잭션 동기화가 활성화되고, TransactionSynchronizationManager에 바인딩됨
        final var connection = DataSourceUtils.getConnection(dataSource);

        try {
            // 트랜잭션 시작
            connection.setAutoCommit(false);

            // 비즈니스 로직 처리
            // DAO는 Connection을 파라미터로 받지 않음
            // JdbcTemplate이 내부적으로 DataSourceUtils를 통해 바인딩된 Connection을 사용
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

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
