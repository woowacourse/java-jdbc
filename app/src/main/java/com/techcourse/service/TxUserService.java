package com.techcourse.service;

import com.interface21.transaction.support.TransactionalTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {
    private final UserService userService;
    private final TransactionalTemplate transactionalTemplate;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionalTemplate = new TransactionalTemplate(DataSourceConfig.getInstance());
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
    public void changePassword(final long id, final String password, final String createBy) {
        transactionalTemplate.execute(transaction -> userService.changePassword(id, password, createBy));
    }
}
