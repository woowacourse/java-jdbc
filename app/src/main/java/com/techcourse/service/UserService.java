package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.util.SQLExceptionUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

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

    public User findByAccount(final String account) {
        return userDao.findByAccount(account);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeWithinTransaction(connection -> {
            final User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }

    private void executeWithinTransaction(Consumer<Connection> consumer) {
        final Connection connection = SQLExceptionUtil.handleSQLException(() -> dataSource.getConnection());
        try {
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            SQLExceptionUtil.handleSQLException(() -> connection.rollback());
            throw new DataAccessException(e);
        } finally {
            SQLExceptionUtil.handleSQLException(() -> connection.close());
        }
    }
}
