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
        User user = appUserService.findById(id);
        TransactionExecutorUtils.releaseActiveConn();
        return user;
    }

    @Override
    public User findByAccount(final String account) {
        User user = appUserService.findByAccount(account);
        TransactionExecutorUtils.releaseActiveConn();
        return user;
    }

    @Override
    public void insert(final User user) {
        appUserService.insert(user);
        TransactionExecutorUtils.releaseActiveConn();
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionExecutorUtils.executeInTransaction(
                () -> appUserService.changePassword(id, newPassword, createBy)
        );
    }
}
