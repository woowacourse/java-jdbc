package com.techcourse.service;

import com.interface21.transaction.JdbcTransactionManager;
import com.techcourse.domain.User;
import java.util.Optional;

public class TxUserService implements UserService {

    private final UserService userService;
    private final JdbcTransactionManager transactionManager;

    public TxUserService(UserService userService, JdbcTransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public Optional<User> findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
