package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

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

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 아이디의 사용자가 존재하지 않습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (
                Connection conn = dataSource.getConnection();
        ) {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userDao.update(user, conn);
            userHistoryDao.log(new UserHistory(user, createBy));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
