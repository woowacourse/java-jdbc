package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.support.Transaction;

public class TxUserService implements UserService {

    private final UserService userService;
    private final Transaction transaction;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transaction = new Transaction(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(final long id) {
        return transaction.run(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transaction.run(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, String newPassword, final String createBy) {
        transaction.run(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
