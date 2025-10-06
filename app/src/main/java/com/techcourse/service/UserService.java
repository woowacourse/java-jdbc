package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.SqlExecutionException;
import com.interface21.jdbc.core.TransactionCallback;
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

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
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
        try (Connection connection = dataSource.getConnection()) {
            executeInTransaction(connection, () -> {
                final var user = userDao.findById(id, connection);

                user.changePassword(newPassword);
                userDao.update(user, connection);

                userHistoryDao.log(new UserHistory(user, createBy), connection);
            });
        } catch (SQLException e) {
            throw new SqlExecutionException(e);
        }
    }

    private void executeInTransaction(final Connection connection, final TransactionCallback callback) {
        final boolean originalAutoCommit = getAutoCommit(connection);

        try {
            connection.setAutoCommit(false);
            callback.execute();
            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new SqlExecutionException(e);
        } finally {
            setAutoCommit(connection, originalAutoCommit);
        }
    }

    private boolean getAutoCommit(final Connection connection) {
        try {
            return connection.getAutoCommit();
        } catch (SQLException e) {
            throw new SqlExecutionException(e);
        }
    }

    private void setAutoCommit(final Connection connection, final boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new SqlExecutionException(e);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new SqlExecutionException(e);
        }
    }
}
