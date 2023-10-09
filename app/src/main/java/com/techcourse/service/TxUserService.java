package com.techcourse.service;

import org.springframework.transaction.TransactionManager;

import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService, final TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(final long id) {
        return transactionManager.transact(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionManager.transact(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.transact(() -> userService.changePassword(id, newPassword, createBy));
    }
}
