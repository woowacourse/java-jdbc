package com.techcourse.service;

import com.interface21.jdbc.transaction.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(AppUserService appUserService, TransactionTemplate transactionTemplate) {
        this.appUserService = appUserService;
        this.transactionTemplate = transactionTemplate;
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
    public void changePassword(long id, String newPassword, String createdBy) {
        DataSource dataSource = DataSourceConfig.getInstance();

        transactionTemplate.execute(() -> {
            appUserService.changePassword(id, newPassword, createdBy);
        }, dataSource);
    }
}
