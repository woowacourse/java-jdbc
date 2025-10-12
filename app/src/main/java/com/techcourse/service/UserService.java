package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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
        try (Connection conn = dataSource.getConnection()) {
            return userDao.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(final Connection conn, final User user) {
        userDao.insert(conn, user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(conn, user);

            userHistoryDao.log(conn, new UserHistory(user, createBy));

            conn.commit();
        } catch (DataAccessException | SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new DataAccessException(e);
        }
    }
}
