package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final PlatformTransactionManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        var transactionStatus = getTransactionStatus();
        try {
            User foundUser = userService.findById(id);
            transactionManager.commit(transactionStatus);
            return foundUser;
        } catch (final RuntimeException e) {
            log.info("transaction rollback!");
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    @Override
    public void insert(final User user) {
        var transactionStatus = getTransactionStatus();
        try {
            userService.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (final RuntimeException e) {
            log.info("transaction rollback!");
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        var transactionStatus = getTransactionStatus();
        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        } catch (final RuntimeException e) {
            log.info("transaction rollback!");
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    private TransactionStatus getTransactionStatus() {
        return transactionManager.getTransaction(new DefaultTransactionAttribute());
    }
}
