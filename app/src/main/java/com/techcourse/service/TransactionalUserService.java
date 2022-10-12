package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import nextstep.transaction.DefaultTransactionDefinition;
import nextstep.transaction.PlatformTransactionManager;
import nextstep.transaction.TransactionStatus;

public class TransactionalUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TransactionalUserService(final PlatformTransactionManager transactionManager,
                                    final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            User user = userService.findById(id);
            transactionManager.commit(transaction);
            return user;
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new DataAccessException();
        }
    }

    @Override
    public void insert(final User user) {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.insert(user);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
        }
        transactionManager.commit(transaction);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.changePassword(id, newPassword, createBy);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new DataAccessException(e);
        }
        transactionManager.commit(transaction);
    }
}
