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
        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                final var user = userDao.findById(connection, id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollbackAndRestoreAutoCommit(connection, e);
                throw e instanceof RuntimeException runtimeException ? runtimeException : new DataAccessException(e);
            }
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    /**
     * 롤백에 성공한 경우에만 자동 커밋을 복원한다.
     * 롤백이 실패했는데 복원하면 {@code setAutoCommit(true)}가 미완료 트랜잭션을 커밋할 수 있으므로, 이때는 복원하지 않는다.
     */
    private void rollbackAndRestoreAutoCommit(final Connection connection, final Exception cause) {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }
}
