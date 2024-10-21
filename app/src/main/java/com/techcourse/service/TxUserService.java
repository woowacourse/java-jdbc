package com.techcourse.service;

import com.interface21.jdbc.core.JdbcTransactionManager;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final JdbcTransactionManager jdbcTransactionManager;

    public TxUserService(UserService userService, JdbcTransactionManager jdbcTransactionManager) {
        this.userService = userService;
        this.jdbcTransactionManager = jdbcTransactionManager;
    }

    @Override
    public User findById(long id) {
        return jdbcTransactionManager.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        jdbcTransactionManager.execute(() -> userService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        jdbcTransactionManager.execute(() -> userService.changePassword(id, newPassword, createdBy));
    }
}
