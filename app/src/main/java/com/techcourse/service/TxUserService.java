package com.techcourse.service;

import com.techcourse.domain.User;

import nextstep.jdbc.transaction.TransactionTemplate;

public class TxUserService implements UserService{

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(final UserService userService, TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    public User findById(final long id) {
        return transactionTemplate.doInTransaction(() -> userService.findById(id));
    }

    public void insert(final User user) {
        transactionTemplate.doInTransactionWithNoResult(() -> userService.insert(user));
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.doInTransactionWithNoResult(() -> userService.changePassword(id, newPassword, createBy));
    }
}
