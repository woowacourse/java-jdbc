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

    public UserService(DataSource dataSource, UserDao userDao, UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        executeInTransaction(connection -> {
            userDao.insert(connection, user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeInTransaction(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }

    private void executeInTransaction(Consumer<Connection> execution) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            execution.accept(connection);

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (NullPointerException | SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException();
        }
    }
}
