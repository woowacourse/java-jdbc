package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.TransactionTemplate;

public class TxUserService implements UserService {

    private final AppUserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(final AppUserService userService, final TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public User findById(final long id) {
        return transactionTemplate.executeWithTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionTemplate.executeWithTransaction(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.executeWithTransaction(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
