package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            userService.changePassword(id, newPassword, createBy);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
        transactionManager.commit(transactionStatus);
    }
}
