package com.techcourse.service;

import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TransactionExecutor.doTransaction(() -> {
            userService.changePassword(id, newPassword , createBy);
            return null;
        });
    }
}
