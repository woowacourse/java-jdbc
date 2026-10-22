package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.NoSuchElementException;

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
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. id: " + id));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);

        try (final var connection = dataSource.getConnection()) {
            final var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException cleanupException) {
                    e.addSuppressed(cleanupException);
                }
                throw e;
            }

            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
