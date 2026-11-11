package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
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

    public void changePassword(final long id, final String newPassword, final String createdBy) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("A transaction is already active for this DataSource");
        }
        try (var connection = DataSourceUtils.getConnection(dataSource)) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, createdBy));
                connection.commit();
            } catch (SQLException | RuntimeException | Error failure) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException failure) {
            throw new DataAccessException(failure);
        }
    }
}
