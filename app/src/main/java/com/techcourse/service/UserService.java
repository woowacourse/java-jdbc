package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.transaction.support.TransactionTemplate;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionTemplate transactionTemplate;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, TransactionTemplate transactionTemplate) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionTemplate = transactionTemplate;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        transactionTemplate.execute(() -> {
            var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            return null;
        });
    }
}
