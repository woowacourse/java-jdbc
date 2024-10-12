package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.interface21.jdbc.core.exception.JdbcSQLException;
import com.techcourse.config.DataSourceConfig;
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
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 id인 user가 존재하지 않습니다."));
    }

    public void insert(final User user) {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            userDao.insert(connection, user);
        } catch (SQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            setTransaction(connection, createBy, user);
        } catch (SQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    private void setTransaction(Connection connection, String createBy, User user) throws SQLException {
        try {
            connection.setAutoCommit(false);

            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public User findByAccount(String account) {
        return userDao.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("해당 account인 user가 존재하지 않습니다."));
    }
}
