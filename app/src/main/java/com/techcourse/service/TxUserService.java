package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.TransactionManager;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final TransactionManager transactionManager;

    public TxUserService(final AppUserService appUserService, final TransactionManager transactionManager) {
        this.appUserService = appUserService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(final long id) {
        return appUserService.findById(id);
    }

    @Override
    public void save(final User user) {
        try {
            transactionManager.begin();
            appUserService.save(user);
            transactionManager.commit();
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            transactionManager.begin();
            appUserService.changePassword(id, newPassword, createBy);
            transactionManager.commit();
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        }
    }
}
