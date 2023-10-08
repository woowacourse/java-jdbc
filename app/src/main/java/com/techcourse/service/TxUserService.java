package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionManager;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final TransactionManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        try {
            transactionManager.begin();
            final User user = userService.findById(id);
            transactionManager.commit();
            return user;
        } catch (final Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            transactionManager.clear();
        }
    }

    @Override
    public void insert(final User user) {
        try {
            transactionManager.begin();
            userService.insert(user);
            transactionManager.commit();
        } catch (final Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            transactionManager.clear();
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            transactionManager.begin();
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit();
        } catch (final Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            transactionManager.clear();
        }
    }
}
