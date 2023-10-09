package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.TransactionManager;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final UserService appUserService;

    public TxUserService(TransactionManager transactionManager, UserService appUserService) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    @Override
    public User findById(long id) {
        return transactionManager.execute(() -> appUserService.findById(id), true);
    }

    @Override
    public void insert(User user) {
        transactionManager.execute(() -> {
            appUserService.insert(user);
            return null;
        }, false);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.execute(() -> {
            appUserService.changePassword(id, newPassword, createBy);
            return null;
        }, false);
    }
}
