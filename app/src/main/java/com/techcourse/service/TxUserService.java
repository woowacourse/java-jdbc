package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.transaction.TransactionExecutor;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionExecutor transactionExecutor;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionExecutor = new TransactionExecutor(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(final Long id) {
        return transactionExecutor.readExecute(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionExecutor.execute(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final Long id, final String newPassword, final String createBy) {
        transactionExecutor.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
