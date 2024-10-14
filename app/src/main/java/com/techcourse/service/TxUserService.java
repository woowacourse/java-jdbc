package com.techcourse.service;

import com.interface21.transaction.TransactionManager;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final AppUserService appUserService;

    public TxUserService(TransactionManager transactionManager, AppUserService appUserService) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    @Override
    public User getById(long id) {
        return appUserService.getById(id);
    }

    @Override
    public void insert(User user) {
        appUserService.insert(user);
    }


    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.performTransaction(connection -> appUserService.changePassword(id, newPassword, createBy));
    }
}
