package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import nextstep.dao.exception.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final PlatformTransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao,
                       final PlatformTransactionManager transactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User findById(final long id) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            User user = userDao.findById(id);
            transactionManager.commit(transactionStatus);
            return user;
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }

    public void insert(final User user) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userDao.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            e.printStackTrace();
            throw new DataAccessException();
        }
    }
}
