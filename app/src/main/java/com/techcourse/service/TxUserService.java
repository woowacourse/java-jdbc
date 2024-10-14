package com.techcourse.service;

import com.interface21.jdbc.core.JdbcTransactionTemplate;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final JdbcTransactionTemplate transactionTemplate;

    public TxUserService(AppUserService appUserService, JdbcTransactionTemplate transactionTemplate) {
        this.appUserService = appUserService;
        this.transactionTemplate = transactionTemplate;
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
        transactionTemplate.executeTransactional(connection ->
                appUserService.changePassword(id, newPassword, createBy)
        );
    }
}
