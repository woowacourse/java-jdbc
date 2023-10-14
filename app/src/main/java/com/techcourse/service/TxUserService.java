package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final AppUserService userService;

    public TxUserService(final DataSource dataSource, final AppUserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        TransactionSynchronizationManager.startNewTransaction(dataSource);

        final User findUser = userService.findById(id);

        TransactionSynchronizationManager.finishTransaction(dataSource);

        return findUser;
    }

    @Override
    public void insert(final User user) {
        TransactionSynchronizationManager.startNewTransaction(dataSource);

        userService.insert(user);

        TransactionSynchronizationManager.finishTransaction(dataSource);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionSynchronizationManager.startNewTransaction(dataSource);

        userService.changePassword(id, newPassword, createBy);

        TransactionSynchronizationManager.finishTransaction(dataSource);
    }
}
