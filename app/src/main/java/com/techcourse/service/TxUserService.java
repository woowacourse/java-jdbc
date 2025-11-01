package com.techcourse.service;

import com.interface21.transaction.TransactionManager;
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
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionManager.executeTransaction(connection -> {
            if (userService instanceof AppUserService) {
                ((AppUserService) userService).changePassword(connection, id, newPassword, createdBy);
            } else {
                userService.changePassword(id, newPassword, createdBy);
            }
        });
    }
}
