package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.TransactionExecutor;

import java.util.Optional;

public class TxAppUserService implements UserService {

    private final AppUserService appUserService;
    private final TransactionExecutor transactionExecutor;

    public TxAppUserService(final AppUserService appUserService, final TransactionExecutor transactionExecutor) {
        this.appUserService = appUserService;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public Optional<User> findById(final long id) {
        return transactionExecutor.execute(() -> appUserService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionExecutor.execute(() -> {
            appUserService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionExecutor.execute(() -> {
            appUserService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
