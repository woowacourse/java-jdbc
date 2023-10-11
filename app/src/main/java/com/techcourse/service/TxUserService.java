package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.TransactionTemplate;

import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(AppUserService appUserService, DataSource dataSource) {
        this.appUserService = appUserService;
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    public User findById(final long id) {
        return transactionTemplate.transaction(() -> appUserService.findById(id));
    }

    public void insert(final User user) {
        transactionTemplate.transaction(() -> appUserService.insert(user));
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.transaction(() -> appUserService.changePassword(id, newPassword, createBy));
    }
}
