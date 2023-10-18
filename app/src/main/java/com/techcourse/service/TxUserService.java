package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionManager;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(TransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return transactionManager.query(() -> userService.findById(id));
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
