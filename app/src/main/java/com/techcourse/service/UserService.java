package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.connection.ConnectionManager;
import org.springframework.dao.DataAccessException;
import java.sql.Connection;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final ConnectionManager connectionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final ConnectionManager connectionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.connectionManager = connectionManager;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        final Connection connection = connectionManager.getConnection(false);
        try {
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            connection.commit();
        } catch (final Exception exception) {
            connectionManager.rollback(connection);
            throw new DataAccessException(exception.getMessage());
        } finally {
            connectionManager.close(connection);
        }
    }
}
