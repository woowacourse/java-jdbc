package com.techcourse.service;

import com.interface21.jdbc.core.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionManager = new TransactionManager(DataSourceConfig.getInstance());
    }

    public User findById(final long id) {
        return transactionManager.manage(conn -> {
            return userService.findById(id);
        });
    }

    public void insert(final User user) {
        transactionManager.manage(conn -> {
            userService.insert(user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.manage(conn -> {
            userService.changePassword(id, newPassword, createBy);
        });
    }
}
