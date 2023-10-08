package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        try (final Connection connection = DataSourceUtils.getConnection(dataSource)) {
            try {
                connection.setAutoCommit(false);

                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.updatePassword(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));

                connection.commit();
            } catch (final SQLException | DataAccessException e) {
                connection.rollback();
                throw new DataAccessException(e);
            } finally {
                connection.setAutoCommit(true);
                DataSourceUtils.releaseConnection(connection, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
