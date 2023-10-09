package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.transaction.TransactionExecutor;

public class TxUserService {

    private final UserService userService;
    private final TransactionExecutor transactionExecutor;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionExecutor = new TransactionExecutor(DataSourceConfig.getInstance());
    }

    public User findById(final long id) {
        return userService.findById(id);
    }

    public void insert(final User user) {
        transactionExecutor.execute(() -> userService.insert(user));
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionExecutor.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
