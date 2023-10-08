package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
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
import java.sql.SQLTransactionRollbackException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource = DataSourceConfig.getInstance();
    private static final int QUERY_SINGLE_SIZE = 1;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            User user = userDao.findById(connection, id);
            connection.commit();
            return user;
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLTransactionRollbackException(e.getMessage());
        }
    }

    public void insert(User user) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            userDao.insert(connection, user);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLTransactionRollbackException(e.getMessage());
        }
    }

    public void changePassword(long id, String newPassword, String createBy) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            User user = findById(id);
            user.changePassword(newPassword);
            updateUser(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLTransactionRollbackException(e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void updateUser(Connection connection, User user) {
        int updateSize = userDao.update(connection, user);
        if (updateSize != QUERY_SINGLE_SIZE) {
            throw new DataAccessException("갱신된 데이터의 개수가 올바르지 않습니다.");
        }
    }
}
