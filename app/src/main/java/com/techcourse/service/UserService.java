package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow();
    }

    public User findByAccount(final String account) {
        return userDao.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection conn = userDao.getDataSource().getConnection()) {
            conn.setAutoCommit(false);

            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));

            conn.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
