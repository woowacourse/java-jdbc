package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return manageTransaction(connection -> {
            return userDao.findById(connection, id);
        });
    }

    public void insert(final User user) {
        manageTransaction(connection -> {
            userDao.insert(connection, user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        manageTransaction(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }

    private <T> T manageTransaction(Function<Connection, T> businessLogic) {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            T result = businessLogic.apply(connection);
            connection.commit();
            return result;
        }catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        }
    }

    private Connection getConnection() {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return connection;
    }

    private void manageTransaction(Consumer<Connection> businessLogic) {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            businessLogic.accept(connection);
            connection.commit();
        }catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        }
    }
}
