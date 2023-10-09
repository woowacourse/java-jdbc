package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.TransactionManager;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final TransactionManager transactionManager;

    public TxUserService(final AppUserService appUserService, final TransactionManager transactionManager) {
        this.appUserService = appUserService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(final long id) {
        return appUserService.findById(id);
    }

    @Override
    public void insert(final User user) {
        appUserService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionManager.doInTransaction(() -> appUserService.changePassword(id, newPassword, createdBy));
    }
}
