package com.techcourse.service;

import com.interface21.transaction.TransactionTemplate;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(AppUserService appUserService, TransactionTemplate transactionTemplate) {
        this.userService = appUserService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionTemplate.execute(() -> {
            userService.changePassword(id, newPassword, createdBy);
            return null;
        });
    }
}
