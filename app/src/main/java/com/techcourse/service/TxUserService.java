package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            User user = userService.findById(id);
            transactionManager.commit(transactionStatus);
            return user;
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insert(User user) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            userService.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new RuntimeException(e);
        }
    }
}
