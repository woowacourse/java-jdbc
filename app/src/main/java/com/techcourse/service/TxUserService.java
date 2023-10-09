package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionExecutor;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionExecutor executor;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.executor = new TransactionExecutor(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(final long id) {
        return executor.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        executor.execute(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executor.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
