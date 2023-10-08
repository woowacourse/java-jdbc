package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.TransactionTemplate;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
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
        transactionTemplate.executeQueryWithTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }
}
