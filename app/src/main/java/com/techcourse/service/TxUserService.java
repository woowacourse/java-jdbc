package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final PlatformTransactionManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        TransactionStatus transactionStatus = transactionManager
                .getTransaction(new DefaultTransactionDefinition());

        try {
            userService.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionStatus transactionStatus = transactionManager
                .getTransaction(new DefaultTransactionDefinition());

        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
