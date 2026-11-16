package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 사용자입니다. id: " + id));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection connection = DataSourceUtils.getConnection(dataSource); // 보관함에 커넥션 등록

        try {
            connection.setAutoCommit(false); // 트랜잭션 시작
            final var user = findById(id);
            user.changePassword(newPassword);
            if (userDao.update(user) == 0) { // DAO는 보관함의 같은 커넥션을 꺼내 씀
                throw new IllegalArgumentException("[ERROR] 존재하지 않는 사용자입니다. id: " + id);
            }
            userHistoryDao.log(new UserHistory(user, createBy));
            connection.commit(); // 둘 다 성공이면 확정
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(connection); // 하나라도 실패라면 취소
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
