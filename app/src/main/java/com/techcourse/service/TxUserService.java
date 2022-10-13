package com.techcourse.service;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.techcourse.domain.User;

import nextstep.jdbc.DataAccessException;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    public User findById(final long id) {
        return userService.findById(id);
    }

    public void insert(final User user) {
        userService.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(status);
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e.getMessage());
        }
    }
}
