package com.techcourse.service;

import com.techcourse.domain.User;
import javax.sql.DataSource;
import org.springframework.transaction.support.TransactionService;

public class TransactionUserService extends TransactionService implements UserService {

    private final UserService userService;

    public TransactionUserService(final UserService userService, final DataSource dataSource) {
        super(dataSource);
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        executeTransaction(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeTransaction(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
