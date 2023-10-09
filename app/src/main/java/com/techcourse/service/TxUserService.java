package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TxUserService implements UserService {

    private final AppUserService userService;

    public TxUserService(final AppUserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        TransactionSynchronizationManager.startTransaction();

        final User findUser = userService.findById(id);

        TransactionSynchronizationManager.finishTransaction();

        return findUser;
    }

    @Override
    public void insert(final User user) {
        TransactionSynchronizationManager.startTransaction();

        userService.insert(user);

        TransactionSynchronizationManager.finishTransaction();
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionSynchronizationManager.startTransaction();

        userService.changePassword(id, newPassword, createBy);

        TransactionSynchronizationManager.finishTransaction();
    }
}
