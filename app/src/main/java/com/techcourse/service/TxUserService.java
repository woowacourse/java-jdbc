package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private UserService userService;
    private PlatformTransactionManager transactionManager;

    public TxUserService(UserService userService, PlatformTransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(long id) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            User user = userService.findById(id);
            transactionManager.commit(transactionStatus);
            return user;
        } catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    @Override
    public void insert(User user) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        } catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }
}
