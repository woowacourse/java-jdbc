package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(DataSourceTransactionManager transactionManager, AppUserService appUserService) {
        this.transactionManager = transactionManager;
        this.userService = appUserService;
    }

    @Override
    public User findById(long id) {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        User user = null;
        try {
            user = userService.findById(id);
            transactionManager.commit(transaction);
        } catch (DataAccessException e) {
            transactionManager.rollback(transaction);
        }
        return user;
    }

    @Override
    public void insert(User user) {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.insert(user);
            transactionManager.commit(transaction);
        } catch (DataAccessException e) {
            transactionManager.rollback(transaction);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transaction);
        } catch (DataAccessException e) {
            transactionManager.rollback(transaction);
            throw new DataAccessException(e);
        }
    }
}
