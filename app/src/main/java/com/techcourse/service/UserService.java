package com.techcourse.service;

import com.interface21.jdbc.datasource.Connection;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.ConnectionSynchronizeManager;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        ConnectionSynchronizeManager.getConnection();
        return userDao.findById(id);
    }

    public void insert(final User user) {
        Connection conn = ConnectionSynchronizeManager.getConnection();
        conn.setAutoCommit(false);
        try {
            userDao.insert(user);

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection conn = ConnectionSynchronizeManager.getConnection();
        conn.setAutoCommit(false);
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }
}
