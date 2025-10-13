package com.techcourse.service;

import com.interface21.jdbc.core.TransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserService {

    private final Connection conn;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final Connection conn, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.conn = conn;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }

    public void changePasswordInTransaction(final long id, final String newPassword, final String createBy) {
        TransactionManager.execute(conn, () -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user, conn);
            userHistoryDao.log(new UserHistory(user, createBy), conn);
            return null;
        });
    }
}
