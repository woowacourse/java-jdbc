package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.transaction.TransactionExecutor;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final TransactionExecutor transactionExecutor;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.transactionExecutor = new TransactionExecutor(DataSourceConfig.getInstance());
    }

    @Override
    public User findbyId(long id) {
        return transactionExecutor.executeTransaction(() -> userService.findbyId(id));
    }

    @Override
    public void insert(User user) {
        transactionExecutor.executeTransaction(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionExecutor.executeTransaction(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
