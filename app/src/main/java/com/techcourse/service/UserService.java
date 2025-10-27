package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
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

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
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
        final DataSource dataSource = DataSourceConfig.getInstance();
        try {
            TransactionSynchronizationManager.bindResource(dataSource, dataSource.getConnection());

            final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
            connection.setAutoCommit(false);
            try {
                final var user = userDao.findById(connection, id);
                user.changePassword(newPassword);
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, createBy));
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
