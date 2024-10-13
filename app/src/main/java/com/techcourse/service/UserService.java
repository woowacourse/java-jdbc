package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.TransactionRollbackException;
import com.techcourse.config.DataSourceConfig;
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

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = dataSource.getConnection()) {
            changeUserPassword(id, newPassword, createBy, connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void changeUserPassword(long id, String newPassword, String createBy, Connection connection) {
        try {
            connection.setAutoCommit(false);

            User user = findById(id);
            user.changePassword(newPassword);

            userDao.update(user, connection);
            userHistoryDao.log(new UserHistory(user, createBy), connection);

            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new TransactionRollbackException(e);
        }
    }
}
