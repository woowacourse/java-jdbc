package com.techcourse.service;

import javax.sql.DataSource;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.config.TransactionManagerConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public TxUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public User findById(final long id) {
        return TransactionManagerConfig.executeInTransaction(dataSource, connection -> {
            return appUserService.findById(id);
        });
    }

    public void insert(final User user) {
        TransactionManagerConfig.executeInTransaction(dataSource, connection -> {
            appUserService.insert(user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionManagerConfig.executeInTransaction(dataSource, connection -> {
            appUserService.changePassword(id, newPassword, createBy);
        });
    }
}
