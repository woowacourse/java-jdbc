package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

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

    public void changePasswordWithTransaction(final long id, final String newPassword, final String createBy) {
        Connection connection = getConnection(userDao.getDataSource());
        try (connection) {
            connection.setAutoCommit(false);

            changePassword(id, newPassword, createBy, connection);

            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException("트랜잭션 수행 중 예외가 발생해 트랜잭션을 rollback 합니다.", e);
        }
    }

    private Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("Connection을 얻는데 실패했습니다.", e);
        }
    }

    private void changePassword(long id, String newPassword, String createBy, Connection connection) {
        User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, createBy));
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("rollback에 실패했습니다.", e);
        }
    }
}
