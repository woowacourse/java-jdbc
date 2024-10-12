package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return executeTransaction(connection -> userDao.findById(connection, id));
    }

    public void insert(final User user) {
        executeTransaction(connection -> {
            userDao.insert(connection, user);
            return null;
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeTransaction(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            return null;
        });
    }

    private <R> R executeTransaction(Function<Connection, R> function) {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            R result = function.apply(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            close(connection);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    private void close(Connection connection) {
        if(connection != null) {
            try {
                connection.close(); // 연결 닫기
            } catch (SQLException closeEx) {
                throw new RuntimeException("Connection close failed", closeEx);
            }
        }
    }
}
