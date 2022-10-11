package com.techcourse.service;

import com.techcourse.domain.User;
import java.util.function.Supplier;
import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final UserService userService;
    private final PlatformTransactionManager transactionManager;

    public TxUserService(final PlatformTransactionManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return executeInTx(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        executeInTx(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeInTx(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }

    private <T> T executeInTx(final Supplier<T> logic) {
        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final T result = logic.get();
            transactionManager.commit(transaction);
            return result;
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            throw new DataAccessException(e);
        } catch (Exception e) {
            transactionManager.commit(transaction);
            throw new DataAccessException(e);
        }
    }
}
