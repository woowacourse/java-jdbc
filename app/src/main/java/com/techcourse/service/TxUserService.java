package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionExecutor;
import org.springframework.transaction.support.TransactionManager;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager = new TransactionExecutor();

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return transactionManager.execute(DataSourceConfig.getInstance(), () -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionManager.execute(DataSourceConfig.getInstance(), () -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.execute(DataSourceConfig.getInstance(), () -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
