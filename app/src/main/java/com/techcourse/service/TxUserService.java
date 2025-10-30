package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.transaction.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final TransactionTemplate transactionTemplate;
    private final AppUserService appUserService;

    public TxUserService(final AppUserService appUserService) {
        this.transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
        this.appUserService = appUserService;
    }

    public User findById(final long id) {
        return appUserService.findById(id);
    }

    public void insert(final User user) {
        appUserService.insert(user);
    }

    public void save(final User user) {
        appUserService.save(user);
    }

    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionTemplate.execute(() -> {
            appUserService.changePassword(id, newPassword, createdBy);
        });
    }
}
