package com.techcourse.service;

import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(long id) {
        return transactionTemplate.executeTransaction(connection -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        transactionTemplate.executeTransaction(connection -> {
            userService.save(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        transactionTemplate.executeTransaction(connection -> {
            userService.changePassword(id, newPassword, createdBy);
            return null;
        });
    }
}
