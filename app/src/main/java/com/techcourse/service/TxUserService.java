package com.techcourse.service;

import com.interface21.transaction.support.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(UserService userService) {
        this.transactionManager = new TransactionManager(DataSourceConfig.getInstance());
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return transactionManager.transaction(() -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        transactionManager.transaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionManager.transaction(() -> userService.changePassword(id, newPassword, createdBy));
    }
}

