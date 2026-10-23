package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.SQLException;

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
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다. id=" + id));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (final var connection = dataSource.getConnection()) {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                final var user = userDao.findById(connection, id)
                        .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다. id=" + id));
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                    throw e;
                }
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException resetException) {
                    e.addSuppressed(resetException);
                }
                throw e;
            }
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
        }
    }
}
