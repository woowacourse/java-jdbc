package com.techcourse.service;

import com.interface21.jdbc.core.TransactionManger;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManger transactionManger;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.transactionManger = new TransactionManger(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(long id) {
        return transactionManger.executeTransaction(connection -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        transactionManger.executeTransaction(connection -> {
            userService.save(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        transactionManger.executeTransaction(connection -> {
            userService.changePassword(id, newPassword, createdBy);
            return null;
        });
    }
}
