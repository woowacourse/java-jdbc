package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.TransactionManager;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final AppUserService userService;

    public TxUserService(final TransactionManager transactionManager, final AppUserService userService) {
        this.transactionManager = transactionManager;
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
        transactionManager.execute(() -> userService.changePassword(id, newPassword, creatBy));
    }
}
