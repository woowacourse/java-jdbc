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
        final DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setReadOnly(true);
        final TransactionStatus transactionStatus = transactionManager.getTransaction(definition);
        try {
            final User user = userService.findById(id);
            transactionManager.commit(transactionStatus);
            return user;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }

    @Override
    public void insert(User user) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            userService.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
