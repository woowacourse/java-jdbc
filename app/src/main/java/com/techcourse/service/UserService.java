package com.techcourse.service;

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
    private final DataSource dataSource;


    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        try (final Connection conn = dataSource.getConnection()) {
            return userDao.findById(conn, id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void insert(final User user) {
        try (final Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                userDao.insert(conn, user);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (final Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                final User user = userDao.findById(conn, id);
                user.changePassword(newPassword);
                userDao.update(conn, user);
                userHistoryDao.log(conn, new UserHistory(user, createBy));
                conn.commit();
            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();
                throw e;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
