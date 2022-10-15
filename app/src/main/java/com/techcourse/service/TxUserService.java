package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;
    private final TransactionStatus transactionStatus;


    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
        this.transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
    }

    @Override
    public User findById(long id) {
        try {
            User user = userService.findById(id);
            transactionManager.commit(transactionStatus);
            return user;
        }
        catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }

    }

    @Override
    public void insert(User user) {
        try {
            userService.insert(user);
            transactionManager.commit(transactionStatus);
        }
        catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        }
        catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }
}
