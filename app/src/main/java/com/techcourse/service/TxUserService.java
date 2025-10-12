package com.techcourse.service;

import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.domain.User;

public class TxUserService implements UserService{

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(UserService userService, TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        transactionTemplate.execute(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
