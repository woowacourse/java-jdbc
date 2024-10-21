package com.techcourse.service;

import com.interface21.jdbc.core.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService targetUserService;
    private final TransactionManager transactionManager = new TransactionManager(DataSourceConfig.getInstance());

    public TxUserService(UserService targetUserService) {
        this.targetUserService = targetUserService;
    }

    @Override
    public User findById(long id) {
        return transactionManager.getResultInTransaction(() -> targetUserService.findById(id));
    }

    @Override
    public void save(User user) {
        transactionManager.executeInTransaction(() -> targetUserService.save(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        transactionManager.executeInTransaction(() -> targetUserService.changePassword(id, newPassword, createdBy));
    }
}
