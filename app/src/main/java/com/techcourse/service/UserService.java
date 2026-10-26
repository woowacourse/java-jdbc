package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public User findById(final Connection conn, final long id) {
        return userDao.findById(conn, id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy){
        Connection conn = null;
        try {
            conn = DataSourceConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            final var user = findById(conn, id);
            user.changePassword(newPassword);
            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));

            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(e);
            }
            throw new DataAccessException(e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
