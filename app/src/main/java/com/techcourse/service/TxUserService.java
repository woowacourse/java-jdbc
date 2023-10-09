package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionManager;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return TransactionManager.serviceForObject(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        TransactionManager.serviceForUpdate(() -> userService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TransactionManager.serviceForUpdate(() -> userService.changePassword(id, newPassword, createBy));
    }
}
