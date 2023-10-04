package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.Transaction;
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
        Transaction transaction = transactionManager.getTransaction();
        transaction.start();

        try {
            userDao.insert(transaction.getConnection(), user);
            transaction.commit();
        } catch (DataAccessException e) {
            transaction.rollback();
            throw e;
        }
    }

    public void changePassword(long id, String newPassword, String createBy) {
        Transaction transaction = transactionManager.getTransaction();
        transaction.start();
        try {
            Connection connection = transaction.getConnection();
            User user = findById(id);

            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            transaction.commit();
        } catch (DataAccessException e) {
            transaction.rollback();
            throw e;
        }
    }

}
