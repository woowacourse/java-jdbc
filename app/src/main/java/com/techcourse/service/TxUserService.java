package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.TransactionTemplate;

public class TxUserService implements UserService {

    private final TransactionTemplate transactionTemplate;
    private final UserService userService;

    public TxUserService(final TransactionTemplate transactionTemplate, final UserService userService) {
        this.transactionTemplate = transactionTemplate;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return transactionTemplate.doTransaction(() ->
            userService.findById(id)
        );
    }

    @Override
    public void insert(final User user) {
        transactionTemplate.doTransaction(() ->
            userService.insert(user)
        );
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.doTransaction(() ->
            userService.changePassword(id, newPassword, createBy)
        );
    }
}
