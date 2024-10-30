package com.techcourse.service;

import com.techcourse.domain.User;

import java.util.Optional;

import static com.techcourse.support.transaction.TransactionExecutor.executeTransaction;

public class TransactionUserService implements UserService {

    private final AppUserService userService;

    public TransactionUserService(AppUserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<User> findById(long id) {
        return userService.findById(id);
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return userService.findByAccount(account);
    }

    @Override
    public void save(User user) {
        executeTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }
}
