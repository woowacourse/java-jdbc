package com.techcourse.service;

import com.techcourse.domain.User;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        final var transactionManager = new DataSourceTransactionManager(dataSource);
        final var transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            userService.insert(user);
            transactionManager.commit(transaction);
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            throw e;
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var transactionManager = new DataSourceTransactionManager(dataSource);
        final var transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transaction);
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            throw e;
        }
    }
}
