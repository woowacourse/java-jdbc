package com.techcourse.service;

import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final AppUserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final AppUserService userService, final TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(final long id) {
        return transactionManager.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionManager.execute(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.execute(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
