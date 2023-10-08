package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.transaction.support.TransactionManager;

import java.sql.Connection;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    private final TransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final TransactionManager transactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.execute((conn) -> changePasswordInTransaction(id, newPassword, createBy, conn));
    }

    private void changePasswordInTransaction(final long id, final String newPassword, final String createBy, final Connection conn) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(conn, user);
        userHistoryDao.log(conn, new UserHistory(user, createBy));
    }
}
