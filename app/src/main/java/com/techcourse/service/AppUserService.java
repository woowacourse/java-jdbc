package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;

public class AppUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public AppUserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return userDao.findById(id);
    }

    @Override
    public void save(final User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        final User user = userDao.transactionFindById(connection, id);
        user.changePassword(newPassword);
        userDao.transactionUpdate(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, createBy));
    }
}
