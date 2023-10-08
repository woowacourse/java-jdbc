package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.TransactionManager;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionManager = new TransactionManager(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        transactionManager.run(() -> {
            userService.insert(user);
            return new Object();
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.run(() -> {
            userService.changePassword(id, newPassword, createBy);
            return new Object();
        });
    }

}
