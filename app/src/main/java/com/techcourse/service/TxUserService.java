package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionManager;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService, TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(long id) {
        return transactionManager.executeWithValue(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionManager.execute(() -> userService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
