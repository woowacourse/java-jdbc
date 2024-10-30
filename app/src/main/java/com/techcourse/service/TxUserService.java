package com.techcourse.service;

import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(UserService userService, TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public User findById(long id) {
        return transactionTemplate.execute((connection) -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionTemplate.executeWithoutResult((connection) -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionTemplate.executeWithoutResult((connection) -> userService.changePassword(id, newPassword, createdBy));
    }
}
