package com.techcourse.service;

import com.interface21.transaction.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TransactionUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TransactionUserService(final UserService userService) {
        this.userService = userService;
        this.transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(final long id) {
        return transactionTemplate.executeReadOnly(() -> userService.findById(id));
    }

    @Override
    public void save(final User user) {
        transactionTemplate.executeWithoutResult(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.executeWithoutResult(() -> userService.changePassword(id, newPassword, createBy));
    }
}
