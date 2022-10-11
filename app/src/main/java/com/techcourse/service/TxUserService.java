package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.exception.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final DataSourceTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final DataSourceTransactionManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public Long insert(final User user) {
        return userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        } catch (final Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
