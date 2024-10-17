package com.techcourse.service;

import com.interface21.jdbc.transaction.TransactionManager;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final DataSource dataSource;

    public TxUserService(AppUserService appUserService, DataSource dataSource) {
        this.appUserService = appUserService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(long id) {
        return appUserService.findById(id);
    }

    @Override
    public void save(User user) {
        appUserService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TransactionManager.executeTransactionOf(
                connection -> appUserService.changePassword(id, newPassword, createBy), dataSource
        );
    }
}
