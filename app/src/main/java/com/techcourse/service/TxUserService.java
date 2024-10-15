package com.techcourse.service;

import com.interface21.transaction.support.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final AppUserService appUserService;

    public TxUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
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
        DataSource dataSource = DataSourceConfig.getInstance();
        TransactionManager.execute(dataSource, () -> appUserService.changePassword(id, newPassword, createBy));
    }
}
