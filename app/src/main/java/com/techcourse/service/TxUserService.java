package com.techcourse.service;

import com.interface21.transaction.TransactionManager;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final TransactionManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionManager.doInTransaction(conn -> userService.changePassword(id, newPassword, createdBy));
    }
}
