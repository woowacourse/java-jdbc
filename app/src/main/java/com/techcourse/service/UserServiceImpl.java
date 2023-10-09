package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserServiceImpl(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(final long id) {
        return userDao.findById(id);
    }

    public User findById(final Connection connection, final long id) {
        return userDao.findById(connection, id);
    }

    @Override
    public void insert(final User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            hi(id, newPassword, createBy);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void hi(final long id, final String newPassword, final String createBy) throws SQLException {
        final var connection = DataSourceConfig.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            final var user = findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (final Exception e) {
            connection.rollback();
            throw new DataAccessException(e);
        } finally {
            connection.close();
        }
    }

}
