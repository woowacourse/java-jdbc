package com.techcourse.service;

import com.interface21.transaction.TransactionManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService, TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }


    @Override
    public User findById(long id) {
        return transactionManager.executeTransactionWithResult(
                connection -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionManager.executeTransactionWithoutResult(
                connection -> userService.insert(user)
        );
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionManager.executeTransactionWithoutResult(
                (connection) -> userService.changePassword(id, newPassword, createdBy));
    }
}
