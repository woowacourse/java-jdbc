package com.techcourse.service;

import com.interface21.transaction.TransactionException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

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

    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeInTransaction(connection -> {
            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);

            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createdBy));
        });
    }

    // TODO. 4단계 - Transaction synchronization 적용하기
    private void executeInTransaction(final Consumer<Connection> action) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            action.accept(connection);

            connection.commit();
        } catch (final RuntimeException | Error ex) {
            rollbackSafely(connection);
            throw ex;
        } catch (final SQLException e) {
            rollbackSafely(connection);
            throw new TransactionException("SQL error during transaction", e);
        } finally {
            closeSafely(connection);
        }
    }

    private void rollbackSafely(final Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (final SQLException rollbackEx) {
                throw new TransactionException("Rollback failed after transaction error", rollbackEx);
            }
        }
    }

    private void closeSafely(final Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException closeEx) {
                throw new TransactionException("Failed to close connection after transaction", closeEx);
            }
        }
    }
}
