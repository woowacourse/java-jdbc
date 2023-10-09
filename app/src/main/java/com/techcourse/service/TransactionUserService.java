package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.TransactionManager;

public class TransactionUserService implements UserService {


    private static final TransactionManager transactionManager = new TransactionManager(DataSourceConfig.getInstance());

    private final UserService userService;

    public TransactionUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return transactionManager.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionManager.execute(() -> userService.insert(user));
    }

    @Override
    public void changePassword(
            final long id,
            final String newPassword,
            final String createBy) {
        transactionManager.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
