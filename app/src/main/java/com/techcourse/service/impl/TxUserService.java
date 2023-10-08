package com.techcourse.service.impl;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.service.UserService;
import org.springframework.transaction.support.ServiceExecutor;

public class TxUserService implements UserService {

    private final UserService userService;
    private final ServiceExecutor serviceExecutor;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.serviceExecutor = new ServiceExecutor(DataSourceConfig.getInstance());
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
        serviceExecutor.execute(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
