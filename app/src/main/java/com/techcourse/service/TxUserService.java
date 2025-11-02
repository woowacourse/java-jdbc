package com.techcourse.service;

import com.techcourse.support.jdbc.CustomTransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final CustomTransactionManager customTransactionManager = new CustomTransactionManager(DataSourceConfig.getInstance());

    public TxUserService(final AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public User findById(final long id) {
        return appUserService.findById(id);
    }

    @Override
    public void save(final User user) {
        appUserService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        customTransactionManager.executeTransaction(() -> {
            appUserService.changePassword(id, newPassword, createBy);
        });
    }
}
