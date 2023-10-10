package com.techcourse.service;

import com.techcourse.domain.User;
import javax.sql.DataSource;
import org.springframework.transaction.TransactionManager;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        transactionManager = new TransactionManager(dataSource);
    }

    @Override
    public User findById(final long id) {
        return transactionManager.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionManager.execute(() -> userService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
