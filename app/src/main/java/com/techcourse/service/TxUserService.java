package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        final var transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            User user = userService.findById(id);
            transactionManager.commit(transactionStatus);
            return user;
        }
        catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }

    }

    @Override
    public void insert(User user) {
        final var transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.insert(user);
            transactionManager.commit(transactionStatus);
        }
        catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        final var transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        }
        catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }
}
