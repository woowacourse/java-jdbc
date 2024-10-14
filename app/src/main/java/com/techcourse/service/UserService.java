package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public Optional<User> findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        User user = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저:" + id));
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);

        try {
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException exception) {
                throw new DataAccessException("트랜잭션 롤백 실패", e);
            }
            throw new DataAccessException("트랜잭션 수행 실패", e);
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (CannotGetJdbcConnectionException e) {
                throw new DataAccessException("Connection 해제 실패", e);
            }
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
