package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import nextstep.jdbc.support.TransactionService;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionUserService extends TransactionService implements UserService {

    private final UserService userService;

    public TransactionUserService(final UserService userService) {
        super(DataSourceConfig.getInstance(), new DefaultTransactionDefinition());
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        runWithTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        runWithTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }
}
