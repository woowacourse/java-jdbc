package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.TransactionManager;

public final class TxUserService implements UserService{

    private final TransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final TransactionManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return transactionManager.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionManager.execute(() -> userService.insert(user));
    }

    @Override
    public void changePassword(long id, final String newPassword, final String createBy) {
        transactionManager.execute(()-> userService.changePassword(id, newPassword, createBy));
    }
}
