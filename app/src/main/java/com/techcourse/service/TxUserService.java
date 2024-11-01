package com.techcourse.service;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TxManager.run(() -> userService.changePassword(id, newPassword, createBy));
    }

    @Override
    public void save(User user) {
        TxManager.run(() -> userService.save(user));
    }

    @Override
    public User findById(long id) {
        return TxManager.run(() -> userService.findById(id));
    }
}
