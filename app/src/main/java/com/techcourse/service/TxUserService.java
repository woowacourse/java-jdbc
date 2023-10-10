package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionManager;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService, final TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    public User findById(final long id) {
        return execute(() -> userService.findById(id));
    }

    public void insert(final User user) {
        execute(() -> {
            userService.insert(user);
            return null;
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        execute(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }

    private <T> T execute(final TransactionCallback<T> transactionCallback) {
        try {
            transactionManager.begin();
            final var result = transactionCallback.callback();
            transactionManager.commit();
            return result;
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        } finally {
            transactionManager.end();
        }
    }
}
