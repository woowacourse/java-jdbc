package com.techcourse.service;

import com.interface21.transaction.support.TransactionManager;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService, TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    public User findById(long id) {
        return transactionManager.beginTransaction(() -> userService.findById(id));
    }

    public void insert(User user) {
        transactionManager.beginTransaction(() -> userService.insert(user));
    }

    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.beginTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }
}
