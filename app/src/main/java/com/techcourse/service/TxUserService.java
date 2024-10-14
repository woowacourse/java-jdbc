package com.techcourse.service;

import com.techcourse.domain.User;
import com.techcourse.service.transaction.TransactionManager;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final AppUserService appUserService;

    public TxUserService(TransactionManager transactionManager, AppUserService appUserService) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    @Override
    public User findById(long id) {
        return transactionManager.transaction(connection -> {
            return appUserService.findById(id);
        });
    }

    @Override
    public void save(User user) {
        transactionManager.transaction(connection -> {
            appUserService.save(user);
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        transactionManager.transaction(connection -> {
            appUserService.changePassword(id, newPassword, createdBy);
        });
    }
}
