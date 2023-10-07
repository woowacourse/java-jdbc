package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(IllegalArgumentException::new);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        DataSource dataSource = DataSourceConfig.getInstance();

        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            User user = findById(id);

            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            commit(connection);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void commit(final Connection connection) throws SQLException {
        try {
            connection.commit();
        } catch (final SQLException e) {
            rollback(connection, e);
        }
    }

    private void rollback(final Connection connection, final SQLException e) throws SQLException {
        connection.rollback();
        throw new DataAccessException(e);
    }
}
