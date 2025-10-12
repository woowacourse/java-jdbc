package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
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

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeSameConnection((connection) -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }

    private void executeSameConnection(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                consumer.accept(connection);
                connection.commit();
            } catch (RuntimeException e) {
                rollback(connection, e);
                throw e;
            } catch (SQLException e) {
                rollback(connection, e);
                throw new DataAccessException(e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection connection, Exception e) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            e.addSuppressed(ex);
        }
    }
}
