package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionTemplate;

public class TxUserService implements UserService {

    private final TransactionTemplate transactionTemplate;
    private final UserService userService;

    public TxUserService(final TransactionTemplate transactionTemplate,
                         final UserService userService) {
        this.transactionTemplate = transactionTemplate;
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
    public void changePassword(final long id,
                               final String newPassword,
                               final String createBy) {
        transactionTemplate.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
