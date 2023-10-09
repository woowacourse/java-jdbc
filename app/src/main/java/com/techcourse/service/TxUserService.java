package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionManager;

import java.util.Optional;

public class TxUserService implements UserService {
    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService, final TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public Optional<User> findById(long id) {
        return transactionManager.executeInTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionManager.executeInTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.executeInTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }
}
