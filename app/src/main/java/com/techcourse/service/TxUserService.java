package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.TransactionExecutor;

public class TxUserService implements UserService {

    private final TransactionExecutor transactionExecutor;
    private final AppUserService userService;

    public TxUserService(final TransactionExecutor transactionExecutor, final AppUserService userService) {
        this.transactionExecutor = transactionExecutor;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String creatBy) {
        transactionExecutor.execute(() -> userService.changePassword(id, newPassword, creatBy));
    }
}
