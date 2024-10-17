package com.techcourse.service;

import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService appUserService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(UserService appUserService, TransactionTemplate transactionTemplate) {
        this.appUserService = appUserService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void insert(final User user) {
        appUserService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        transactionTemplate.execute(connection -> appUserService.changePassword(id, newPassword, createdBy));
    }

    @Override
    public User getById(long id) {
        return appUserService.getById(id);
    }
}
