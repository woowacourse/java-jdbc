package com.techcourse.service;

import com.techcourse.config.TransactionManagerConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final AppUserService appUserService;

    public TxUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public User findById(final long id) {
        return TransactionManagerConfig.executeInTransaction(connection -> {
            return appUserService.findById(id);
        });
    }

    public void insert(final User user) {
        TransactionManagerConfig.executeInTransaction(connection -> {
            appUserService.insert(user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionManagerConfig.executeInTransaction(connection -> {
            appUserService.changePassword(id, newPassword, createBy);
        });
    }
}
