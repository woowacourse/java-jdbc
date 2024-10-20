package com.techcourse.service;

import com.techcourse.domain.User;
import com.techcourse.support.jdbc.transaction.TransactionExecutorUtils;

public class TxUserService implements UserService {

    private final AppUserService appUserService;

    public TxUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public User findById(final long id) {
        return appUserService.findById(id);
    }

    @Override
    public User findByAccount(final String account) {
        return appUserService.findByAccount(account);
    }

    @Override
    public void insert(final User user) {
        appUserService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionExecutorUtils.executeInTransaction(
                () -> appUserService.changePassword(id, newPassword, createBy)
        );
    }
}
