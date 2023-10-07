package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class UserService {

    private final DataSource dataSource = DataSourceConfig.getInstance();
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID에 해당하는 유저가 존재하지 않습니다."));
    }

    public User findByAccount(final String account) {
        return userDao.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("해당 Account에 해당하는 유저가 존재하지 않습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            commit(connection);
        } catch (final SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private void commit(final Connection connection) {
        try {
            connection.commit();
            connection.close();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
            connection.close();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
