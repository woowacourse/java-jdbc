package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.Transaction;

public class TransactionUserService implements UserService {

    private static final Transaction TRANSACTION = new Transaction(DataSourceConfig.getInstance());

    private final UserService userService;

    public TransactionUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return TRANSACTION.run(connection -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        TRANSACTION.run(connection -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id,
                               final String newPassword,
                               final String createBy) {
        TRANSACTION.run(connection -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
