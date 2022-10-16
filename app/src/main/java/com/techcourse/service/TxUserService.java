package com.techcourse.service;

import com.techcourse.domain.User;
import java.util.function.Supplier;
import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(PlatformTransactionManager transactionManager,
                         UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return executeTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        executeTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    private <T> T executeTransaction(Supplier<T> service) {
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final var queryResult = service.get();
            transactionManager.commit(transactionStatus);
            return queryResult;
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }

    private void executeTransaction(Runnable service) {
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            service.run();
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
