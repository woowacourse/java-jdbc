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
        return transactionManager.execute(conn -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionManager.execute(conn -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.execute(conn -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
