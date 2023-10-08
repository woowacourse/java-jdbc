package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.jdbc.core.TransactionManager;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionManager = new TransactionManager(DataSourceConfig.getInstance());
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
        transactionManager.executeNoReturn(() ->
                userService.changePassword(id, newPassword, createBy)
        );
    }
}
