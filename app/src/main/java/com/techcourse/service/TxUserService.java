package com.techcourse.service;

import com.techcourse.domain.User;
import com.techcourse.support.TransactionTemplate;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.execute(()-> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.execute(()-> userService.changePassword(id, newPassword, createBy));
    }
}
