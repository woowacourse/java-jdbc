package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(
            final UserDao userDao,
            final UserHistoryDao userHistoryDao,
            final DataSource dataSource
    ) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) throws SQLException {
        return userDao.findById(dataSource.getConnection(), id)
                .orElseThrow(() -> new NoSuchElementException("해당 아이디의 사용자가 존재하지 않습니다."));
    }

    public void insert(final User user) throws SQLException {
        final Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        try {
            userDao.insert(conn, user);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw new DataAccessException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        final var user = findById(id);
        user.changePassword(newPassword);
        try {
            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));

            conn.commit();
        } catch (SQLException | DataAccessException e) {
            conn.rollback();
            throw new DataAccessException(e);
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
