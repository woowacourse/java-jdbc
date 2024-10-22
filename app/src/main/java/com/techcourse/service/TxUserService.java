package com.techcourse.service;

import com.interface21.transaction.TransactionManager;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService, TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(long id) {
        return transactionManager.runInTransaction(() -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        transactionManager.runInTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        transactionManager.runInTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }
}
