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

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final DataSource dataSource) {
        this(new UserDao(dataSource), new UserHistoryDao(dataSource), dataSource);
    }

    UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
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
        boolean committed = false;
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                updatePasswordAndLogHistory(connection, id, newPassword, createBy);
                connection.commit();
                committed = true;
            } catch (SQLException | RuntimeException | Error failure) {
                rollback(connection, failure);
                throw failure;
            }
        } catch (SQLException exception) {
            if (committed) {
                throw new PasswordChangeCommittedException(exception);
            }
            throw new DataAccessException("비밀번호 변경 트랜잭션에 실패했습니다.", exception);
        }
    }

    private void updatePasswordAndLogHistory(final Connection connection, final long id,
                                             final String newPassword, final String createBy) {
        final var user = userDao.findById(connection, id);
        user.changePassword(newPassword);
        userDao.update(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, createBy));
    }

    private void rollback(final Connection connection, final Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }
}
