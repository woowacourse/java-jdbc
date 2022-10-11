package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final PlatformTransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
    }

    public User findById(final long id) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final User user = userDao.findById(id);
            transactionManager.commit(transactionStatus);
            return user;
        } catch (Exception exception) {
            transactionManager.rollback(transactionStatus);
            throw exception;
        }
    }

    public void insert(final User user) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userDao.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (Exception exception) {
            transactionManager.rollback(transactionStatus);
            throw exception;
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            transactionManager.commit(transactionStatus);
        } catch (Exception exception) {
            transactionManager.rollback(transactionStatus);
            throw exception;
        }
    }
}
