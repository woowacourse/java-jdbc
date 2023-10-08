package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.TransactionManager;

public class TransactionUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TransactionUserService(UserService userService, TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
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
        transactionManager.execute(conn -> userService.changePassword(id, newPassword, createBy));
    }
}
