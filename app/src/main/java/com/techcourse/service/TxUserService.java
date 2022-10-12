package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final AppUserService appUserService;

    public TxUserService(final PlatformTransactionManager transactionManager, final AppUserService appUserService) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    @Override
    public User findById(final long id) {
        return appUserService.findById(id);
    }

    @Override
    public void insert(final User user) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            transactionManager.commit(status);
            appUserService.insert(user);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException();
        }
    }

    @Override
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
