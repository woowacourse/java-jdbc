package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.DaoMethodExecutor;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        DaoMethodExecutor.executeConsumerInTx(getConnection(), connection -> {
            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }

    private Connection getConnection() {
        try {
            return DataSourceConfig.getInstance().getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void insert(final User user) {
        DaoMethodExecutor.executeConsumer(getConnection(), connection -> userDao.insert(connection, user));
    }

    public User findById(final long id) {
        return DaoMethodExecutor.executeFunction(getConnection(), connection -> userDao.findById(connection, id));
    }
}
