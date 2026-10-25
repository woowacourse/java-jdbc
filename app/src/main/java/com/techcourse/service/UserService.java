package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
        this.userDao = Objects.requireNonNull(userDao, "userDao must not be null");
        this.userHistoryDao = Objects.requireNonNull(userHistoryDao, "userHistoryDao must not be null");
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (final var connection = dataSource.getConnection()) {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                final var user = userDao.findById(connection, id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                rollbackAndRestore(connection, autoCommit, e);
                throw e;
            }

            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to change password in a transaction", e);
        }
    }

    private void rollbackAndRestore(final Connection connection, final boolean autoCommit, final Throwable cause) {
        try {
            connection.rollback();
            // 롤백 성공 후에만 복원한다. 롤백 실패 시 autoCommit=true 변경은 암묵적으로 커밋할 수 있다.
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }
}
