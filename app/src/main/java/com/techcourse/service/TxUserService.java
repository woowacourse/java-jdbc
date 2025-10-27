package com.techcourse.service;

import com.interface21.transaction.TransactionManager;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.transactionManager = new TransactionManager(dataSource);
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionManager.executeTransaction((connection) -> {
            userService.changePassword(id, newPassword, createdBy);
        });
    }
}
