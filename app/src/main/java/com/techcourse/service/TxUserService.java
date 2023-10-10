package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicReference;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(UserService userService, TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public User findById(long id) {
        AtomicReference<User> user = new AtomicReference<>();
        transactionTemplate.execute(() -> user.set(userService.findById(id)));
        return user.get();
    }

    @Override
    public void insert(User user) {
        transactionTemplate.execute(() -> userService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionTemplate.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
