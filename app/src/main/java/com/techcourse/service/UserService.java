package com.techcourse.service;

import com.interface21.jdbc.datasource.ConnectionExecutor;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        ConnectionExecutor.executeTransactional(dataSource, connection -> {
            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }

    public void insert(final User user) {
        ConnectionExecutor.execute(dataSource, connection -> userDao.insert(connection, user));
    }

    public User findById(final long id) {
        return ConnectionExecutor.apply(dataSource, connection -> userDao.findById(connection, id));
    }
}
