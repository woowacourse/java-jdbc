package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionManager;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService, final TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    public User findById(final long id) {
        return userService.findById(id);
    }

    public void insert(final User user) {
        userService.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            transactionManager.begin();
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit();
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        } finally {
            transactionManager.end();
        }
    }
}
