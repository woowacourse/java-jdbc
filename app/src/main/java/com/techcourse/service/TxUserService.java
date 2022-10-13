package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager platformTransactionManager;
    private final UserService userService;

    public TxUserService(final PlatformTransactionManager platformTransactionManager, final UserService userService) {
        this.platformTransactionManager = platformTransactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        executeTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, String newPassword, final String createBy) {
        executeTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    private void executeTransaction(final Runnable runnable) {
        final TransactionStatus transactionStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            runnable.run();
            platformTransactionManager.commit(transactionStatus);
        } catch (Exception e) {
            platformTransactionManager.rollback(transactionStatus);
            throw e;
        }
    }
}
