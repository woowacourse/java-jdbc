package com.techcourse.service;

import com.interface21.transaction.TransactionTemplate;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate = new TransactionTemplate();

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return transactionTemplate.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionTemplate.execute(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionTemplate.execute(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
