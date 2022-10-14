package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        executeTransaction(()->userService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        executeTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    public void executeTransaction(final TransactionExecutor executor) {
        final TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            executor.execute();
            transactionManager.commit(status);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
    }
}
