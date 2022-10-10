package com.techcourse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.techcourse.domain.User;

import nextstep.jdbc.DataAccessException;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

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
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            transactionManager.rollback(status);
            log.error(e.getMessage());
            throw new DataAccessException(e);
        }
    }
}
