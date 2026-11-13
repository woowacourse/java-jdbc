package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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
    private final DataSource dataSource;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, DataSource dataSource) {
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
        Connection conn = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, conn);
        try {
            conn.setAutoCommit(false);
            changePasswordWithHistory(conn, id, newPassword, createBy);
            conn.commit();
        } catch (SQLException e) {
            throw new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void changePasswordWithHistory(Connection conn, long id, String newPassword, String createBy) {
        try {
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.updatePassword(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        } catch (RuntimeException | Error e) {
            rollback(conn, e);
            throw e;
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
