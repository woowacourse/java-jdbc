package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.Connection;
import com.interface21.jdbc.datasource.ConnectionContext;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        getConnection();
        return userDao.findById(id);
    }

    public void insert(final User user) {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        try {
            userDao.insert(user);
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

    private Connection getConnection() {
        try {
            DataSource dataSource = DataSourceConfig.getInstance();
            Connection conn = new Connection(dataSource.getConnection());
            ConnectionContext.conn.set(conn);

            return conn;
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }
}
