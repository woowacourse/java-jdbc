package com.techcourse.service;

import com.techcourse.domain.User;
import com.techcourse.support.transaction.TransactionExecutor;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final TransactionExecutor transactionExecutor;
    private final UserService userService;

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.transactionExecutor = new TransactionExecutor(dataSource);
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionExecutor.execute(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
