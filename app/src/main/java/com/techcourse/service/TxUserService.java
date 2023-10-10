package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionTemplate;

public class TxUserService implements UserService {

    private final TransactionTemplate transactionTemplate;
    private final UserService userService;

    public TxUserService(TransactionTemplate transactionTemplate, UserService userService) {
        this.transactionTemplate = transactionTemplate;
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
        transactionTemplate.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
