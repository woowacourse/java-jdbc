package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionExecutor;

public class TxUserService implements UserService {

    private final UserService appUserService;

    public TxUserService(UserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public User findById(long id) {
        return appUserService.findById(id);
    }

    @Override
    public void insert(User user) {
        appUserService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TransactionExecutor.execute(
            DataSourceConfig.getInstance(),
            () -> {
                appUserService.changePassword(id, newPassword, createBy);
                return null;
            }
        );
    }
}
