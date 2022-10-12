package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(UserService userService) {
        this.transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        executeInTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeInTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    private void executeInTransaction(final TransactionalExecutor executor) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            executor.execute();
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException e) {
            log.error("Transaction is being rolled back");
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }
}
