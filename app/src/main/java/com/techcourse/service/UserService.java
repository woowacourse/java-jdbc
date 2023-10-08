package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return TransactionExecutor.transactionQuery(connection -> userDao.findById(id));
    }

    public void insert(User user) {
        TransactionExecutor.transactionCommand(connection -> userDao.insert(user));
    }

    public void changePassword(long id, String newPassword, String createBy) {
        TransactionExecutor.transactionCommand(
                connection -> {
                    User user = userDao.findById(id);
                    user.changePassword(newPassword);
                    userDao.update(user);
                    userHistoryDao.log(new UserHistory(user, createBy));
                }
        );
    }

}
