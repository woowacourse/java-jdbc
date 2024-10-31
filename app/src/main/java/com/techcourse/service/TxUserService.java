package com.techcourse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.TransactionManager;
import com.interface21.jdbc.exception.TransactionExecutionException;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(final UserService userService, final TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        transactionManager.execute(connection -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            transactionManager.execute(connection -> userService.changePassword(id, newPassword, createBy));
        } catch (final TransactionExecutionException ex) {
            log.info("비밀번호 변경에 실패하였습니다.");
            throw ex;
        }
    }
}
