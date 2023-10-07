package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

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

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(DataSource dataSource, final long id, final String newPassword, final String createBy) {
        Connection conn = getConnection(dataSource);
        try (conn) {
            conn.setAutoCommit(false);
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new DataAccessException(e);
        }
    }

    private Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection conn) {
        try{
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
