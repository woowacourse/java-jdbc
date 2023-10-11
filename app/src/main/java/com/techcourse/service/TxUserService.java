package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import static org.springframework.transaction.support.TransactionTemplate.execute;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return execute(DataSourceConfig.getInstance(), () -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        execute(DataSourceConfig.getInstance(), () -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var dataSource = DataSourceConfig.getInstance();
        execute(dataSource, () -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }

}
