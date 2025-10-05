package com.techcourse.service;

import com.interface21.context.stereotype.Component;
import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;

@Component
public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public Optional<User> findByAccount(final String account) {
        return userDao.findByAccount(account);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection conn = dataSource.getConnection()) {
            try {
                conn.setAutoCommit(false);

                final var user = userDao.findById(id);
                user.changePassword(newPassword);
                userDao.update(conn, user);
                userHistoryDao.log(conn, new UserHistory(user, createBy));

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DataAccessException();
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }
}
