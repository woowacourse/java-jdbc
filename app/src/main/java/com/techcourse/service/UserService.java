package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private static final DataSource dataSource = DataSourceConfig.getInstance();

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        try (final Connection connection = dataSource.getConnection()) {
            return userDao.findById(connection, id);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    public void insert(final User user) {
        try (final Connection connection = dataSource.getConnection()) {
            userDao.insert(connection, user);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            handleChangePasswordTransaction(id, newPassword, createBy, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void handleChangePasswordTransaction(long id, String newPassword, String createBy, Connection connection) throws SQLException {
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        } catch (Exception ex) {
            connection.rollback();
        }
    }
}
