package com.techcourse.service;

import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.domain.User;
import java.util.Optional;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(UserService userService, TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Optional<User> findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.execute(() -> {
            userService.changePassword(id, newPassword, createBy);
        });
    }
}
