package com.techcourse.service;

import com.interface21.transaction.JdbcTransactionManager;
import com.interface21.transaction.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionManager = new JdbcTransactionManager(DataSourceConfig.getInstance());
    }

    public void insert(final User user) {
        transactionManager.doInTransaction(connection -> {
            userService.insert(user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.doInTransaction(connection -> {
            userService.changePassword(id, newPassword, createBy);
        });
    }

    public User findById(final long id) {
        return userService.findById(id);
    }
}
