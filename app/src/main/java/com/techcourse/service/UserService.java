package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
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
        final User user = findById(id);
        user.changePassword(newPassword);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            connection.commit();
        } catch (Throwable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to rollback", ex);
            }
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
