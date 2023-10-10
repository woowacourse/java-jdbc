package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionExecutor;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionExecutor transactionExecutor;

    public TxUserService(final AppUserService appUserService, final TransactionExecutor transactionExecutor) {
        this.userService = appUserService;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public User findById(final long id) {
        return transactionExecutor.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionExecutor.execute(() -> {
            userService.insert(user);
            return null;
        });

    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionExecutor.execute(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
