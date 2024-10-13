package com.techcourse.service;

import com.interface21.transaction.support.TransactionManager;
import com.techcourse.domain.User;

public class TxUserService implements UserService{

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService, TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public void insert(User user) {
        transactionManager.executeInTransaction(
                () -> userService.insert(user)
        );
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.executeInTransaction(
                () -> userService.changePassword(id, newPassword, createBy)
        );
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public User findByAccount(String account) {
        return userService.findByAccount(account);
    }
}
