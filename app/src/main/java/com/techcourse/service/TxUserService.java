package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionManager;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final UserService appUserService;

    public TxUserService(TransactionManager transactionManager, UserService appUserService) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    @Override
    public User findById(long id) {
        return appUserService.findById(id);
    }

    @Override
    public void insert(User user) {
        transactionManager.start();
        try {
            appUserService.insert(user);

            transactionManager.commit();
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw e;
        }

    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.start();
        try {
            appUserService.changePassword(id, newPassword, createBy);

            transactionManager.commit();
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw e;
        }
    }
}
