package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = dataSource.getConnection()) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            boolean transactionCompleted = false;
            Throwable failure = null;
            try {
                changePassword(connection, id, newPassword, createBy);
                connection.commit();
                transactionCompleted = true;
            } catch (SQLException | RuntimeException | Error e) {
                failure = e;
                transactionCompleted = rollback(connection, e);
                throw e;
            } finally {
                // 롤백 실패 시 자동 커밋을 켜면 미완료 변경이 커밋될 수 있다.
                if (transactionCompleted) {
                    restoreAutoCommit(connection, autoCommit, failure);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to change password in transaction", e);
        }
    }

    private void changePassword(final Connection connection, final long id,
                                final String newPassword, final String createBy) {
        final var user = userDao.findById(connection, id);
        user.changePassword(newPassword);
        userDao.update(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, createBy));
    }

    private boolean rollback(final Connection connection, final Throwable failure) {
        try {
            connection.rollback();
            return true;
        } catch (SQLException e) {
            failure.addSuppressed(e);
            return false;
        }
    }

    private void restoreAutoCommit(final Connection connection, final boolean autoCommit,
                                   final Throwable failure) throws SQLException {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            if (failure == null) {
                throw e;
            }
            failure.addSuppressed(e);
        }
    }
}
