package com.techcourse.service;

import com.interface21.transaction.support.TransactionExecutionManager;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
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
        TransactionExecutionManager.executeInTransaction(
            dataSource,
            () -> userService.changePassword(id, newPassword, createdBy)
        );
    }
}
