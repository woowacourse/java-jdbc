package com.techcourse.service;


import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return executeValueWithTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        executeWithTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeWithTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    private void executeWithTransaction(Runnable runnable) {
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            runnable.run();

            transactionManager.commit(transactionStatus);
        } catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeValueWithTransaction(TxExecutor<T> txExecutor) {
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            T result = txExecutor.execute();

            transactionManager.commit(transactionStatus);
            return result;
        } catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
