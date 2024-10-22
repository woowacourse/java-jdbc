package com.techcourse.service;

import com.interface21.jdbc.datasource.ConnectionExecutor;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        ConnectionExecutor.executeTransactional(() -> userService.changePassword(id, newPassword, createBy));
    }

    @Override
    public void insert(User user) {
        ConnectionExecutor.execute(() -> userService.insert(user));
    }

    @Override
    public User findById(long id) {
        return ConnectionExecutor.supply(() -> userService.findById(id));
    }
}
