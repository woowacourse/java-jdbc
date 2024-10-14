package com.techcourse.service;

import com.interface21.jdbc.core.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.transactionManager = new TransactionManager(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(long id) {
        return transactionManager.transactionBegin(connection -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        transactionManager.transactionBegin(connection -> {
            userService.save(user);
            return true;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        transactionManager.transactionBegin(connection -> {
            userService.changePassword(id, newPassword, createdBy);
            return true;
        });
    }
}
