package com.techcourse.service;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.transaction.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final TransactionManager transactionManager;
    private final AppUserService appUserService;

    public TxUserService(
            final TransactionManager transactionManager,
            final AppUserService appUserService
    ) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    public User findById(final long id) {
        return appUserService.findById(id);
    }

    public void insert(final User user) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        transactionManager.beginTransaction(dataSource, connection -> appUserService.insert(user));
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        log.info("[TxUserService] changePassword: id={}, newPassword={}, createBy={}", id, newPassword, createBy);
        final DataSource dataSource = DataSourceConfig.getInstance();

        transactionManager.beginTransaction(dataSource, connection -> {
            appUserService.changePassword(id, newPassword, createBy);
        });
    }
}
