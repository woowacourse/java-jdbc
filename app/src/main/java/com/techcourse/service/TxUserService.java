package com.techcourse.service;

import com.interface21.transaction.support.TransactionManager;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.transactionManager = new TransactionManager(dataSource);
    }

    @Override
    public User findById(long id) {
        return transactionManager.executeMethodWithTransaction(() -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        transactionManager.executeMethodWithTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        transactionManager.executeMethodWithTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }
}
