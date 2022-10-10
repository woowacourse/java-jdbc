package com.techcourse.service;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.techcourse.domain.User;

import nextstep.jdbc.exception.DataAccessException;

public class TxUserService implements UserService {

    private final DataSourceTransactionManager transactionManager;
    private final AppUserService appUserService;

    public TxUserService(DataSourceTransactionManager transactionManager, AppUserService appUserService) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    public User findById(final long id) {
        return appUserService.findById(id);
    }

    public void insert(final User user) {
        appUserService.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            appUserService.changePassword(id, newPassword, createBy);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException();
        }
    }
}
