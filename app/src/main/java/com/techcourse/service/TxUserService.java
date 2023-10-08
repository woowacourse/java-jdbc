package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionHandler;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionHandler transactionHandler;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionHandler = new TransactionHandler(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionHandler.handle(() -> userService.changePassword(id, newPassword, createBy));
    }
}

