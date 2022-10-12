package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final AppUserService appUserService;

    public TxUserService(DataSourceTransactionManager transactionManager,
                         AppUserService appUserService) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    @Override
    public User findById(long id) {
        var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            User result = appUserService.findById(id);
            transactionManager.commit(transactionStatus);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }

    @Override
    public void insert(User user) {
        var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            appUserService.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            appUserService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
