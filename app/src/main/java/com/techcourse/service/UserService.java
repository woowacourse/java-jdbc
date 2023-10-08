package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionManager;

public class UserService {

    private final TransactionManager transactionManager;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(TransactionManager transactionManager, UserDao userDao, UserHistoryDao userHistoryDao) {
        this.transactionManager = transactionManager;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        transactionManager.start();

        try {
            userDao.insert(user);
            transactionManager.commit();
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw e;
        }
    }

    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.start();
        try {
            User user = findById(id);

            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            transactionManager.commit();
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw e;
        }
    }

}
