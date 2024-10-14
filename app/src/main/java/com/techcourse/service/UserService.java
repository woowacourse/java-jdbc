package com.techcourse.service;

import com.interface21.jdbc.transaction.TransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserService {

    private final TransactionManager txManager;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(TransactionManager txManager, UserDao userDao, UserHistoryDao userHistoryDao) {
        this.txManager = txManager;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        txManager.executeTransactionOf(conn -> changePasswordTx(conn, id, newPassword, createBy));
    }

    private void changePasswordTx(Connection connection, long id, String newPassword, String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.updateUsingExplicitConnection(user, connection);
        userHistoryDao.logUsingExplicitConnection(new UserHistory(user, createBy), connection);
    }
}
