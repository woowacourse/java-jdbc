package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionTemplate;

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
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TransactionTemplate.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
