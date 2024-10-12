package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.exception.TransactionFailedException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (final Connection conn = dataSource.getConnection()) {
            changePasswordWithTransaction(id, newPassword, createBy, conn);
        } catch (SQLException e) {
            throw new IllegalStateException("패스워드 변경 실패: 데이터베이스 연결 오류가 발생했습니다.", e);
        }
    }

    private void changePasswordWithTransaction(final long id, final String newPassword, final String createBy, final Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            changePasswordInTransaction(id, newPassword, createBy, conn);
            conn.commit();
        } catch (DataAccessException e) {
            handleRollback(conn);
            throw new TransactionFailedException("트랜잭션 실패: 패스워드 변경 중 오류가 발생했습니다.", e);
        }
    }

    private void changePasswordInTransaction(long id, String newPassword, String createBy, Connection conn) {
        final User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(conn, user);
        userHistoryDao.log(conn, new UserHistory(user, createBy));
    }

    private void handleRollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new TransactionFailedException("트랜잭션 실패: 롤백 중 오류가 발생했습니다.", e);
        }
    }
}
