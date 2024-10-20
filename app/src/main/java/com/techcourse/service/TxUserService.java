package com.techcourse.service;

import com.techcourse.domain.User;
import com.techcourse.support.TransactionExecutor;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return TransactionExecutor.executeInTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        TransactionExecutor.executeInTransaction(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TransactionExecutor.executeInTransaction(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
