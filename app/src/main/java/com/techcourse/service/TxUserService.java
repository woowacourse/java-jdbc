package com.techcourse.service;

import com.techcourse.domain.User;
import com.techcourse.support.TransactionTemplate;

import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionTemplate.executeWithoutResult(() ->
            userService.changePassword(id, newPassword, createdBy)
        );
    }
}
