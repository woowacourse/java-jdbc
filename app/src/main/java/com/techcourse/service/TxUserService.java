package com.techcourse.service;

import com.interface21.jdbc.datasource.TransactionManager;
import com.interface21.transaction.support.TransactionTemplate;
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
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
