package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionTemplate;

public class TxUserService implements UserService {
    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(final AppUserService appUserService, final TransactionTemplate transactionTemplate) {
        this.userService = appUserService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.execute(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
