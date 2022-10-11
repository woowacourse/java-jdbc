package com.techcourse.service;

import com.techcourse.domain.User;
import java.util.function.Supplier;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.exception.JdbcConnectionException;
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
        return runTransactionalBusiness(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        runTransactionalBusiness(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        runTransactionalBusiness(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }

    private <T> T runTransactionalBusiness(final Supplier<T> supplier) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            final T result = supplier.get();
            transactionManager.commit(transactionStatus);
            return result;
        } catch (final DataAccessException |
                       EmptyResultDataAccessException |
                       IncorrectResultSizeDataAccessException |
                       JdbcConnectionException exception) {
            transactionManager.rollback(transactionStatus);
            throw exception;
        }
    }
}
