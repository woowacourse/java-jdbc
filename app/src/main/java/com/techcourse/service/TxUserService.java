package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.dao.exception.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final PlatformTransactionManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return extracted(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        extracted(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        extracted(() -> {
            User user = findById(id);
            user.changePassword(newPassword);
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }

    private <T> T extracted(final TransactionExecutor<T> transactionExecutor) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            T result = transactionExecutor.execute();
            transactionManager.commit(transactionStatus);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }
}
