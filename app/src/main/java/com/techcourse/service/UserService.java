package com.techcourse.service;

import com.interface21.transaction.support.TransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.exception.UserNotFoundException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, TransactionManager transactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException("유저 정보가 존재하지 않습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.injectTransaction(conn -> {
            User user = findById(id);
            User changedUser = user.changePassword(newPassword);
            userDao.update(conn, changedUser);
            userHistoryDao.log(conn, new UserHistory(user, createBy));
        });
    }
}
