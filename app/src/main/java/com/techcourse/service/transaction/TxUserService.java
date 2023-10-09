package com.techcourse.service.transaction;

import com.techcourse.domain.User;
import com.techcourse.service.UserService;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(final UserService userService, final TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        transactionTemplate.executeWithTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.executeWithTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }
}
