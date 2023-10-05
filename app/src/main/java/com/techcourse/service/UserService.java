package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final Connection conn, final User user) {
        userDao.insert(conn, user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        final var user = findById(id);
        try {
            user.changePassword(newPassword);
            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException | DataAccessException e) {
            conn.rollback();
            conn.setAutoCommit(true);
            throw new DataAccessException(e);
        } finally {
            conn.close();
        }

    }
}
