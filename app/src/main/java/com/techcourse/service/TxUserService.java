package com.techcourse.service;

import com.interface21.transaction.support.JdbcTransactionTemplate;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final JdbcTransactionTemplate transactionTemplate;
    private final UserService userService;

    public TxUserService(final JdbcTransactionTemplate transactionTemplate, final UserService userService) {
        this.transactionTemplate = transactionTemplate;
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
        transactionTemplate.execute(() -> userService.changePassword(id, newPassword, createdBy));
    }
}
