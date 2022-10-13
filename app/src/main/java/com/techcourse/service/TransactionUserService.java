package com.techcourse.service;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.techcourse.domain.User;

import nextstep.jdbc.DataAccessException;

public class TransactionUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService target;

    public TransactionUserService(final PlatformTransactionManager transactionManager, final UserService target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    @Override
    public User findById(final long id) {
        return executeTransaction(() -> target.findById(id));
    }

    @Override
    public void save(final User user) {
        executeTransaction(() -> {
            target.save(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeTransaction(() -> {
            target.changePassword(id, newPassword, createBy);
            return null;
        });
    }

    private <T> T executeTransaction(TransactionExecutable<T> executable) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
            new DefaultTransactionDefinition());
        try {
            final T result = executable.execute();
            transactionManager.commit(transactionStatus);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
