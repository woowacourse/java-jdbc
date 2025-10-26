package com.techcourse.service;

import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final TransactionTemplate transactionTemplate;
    private final UserService userService;

    public TxUserService(final TransactionTemplate transactionTemplate, final UserService userService) {
        this.transactionTemplate = transactionTemplate;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return transactionTemplate.execute(connection -> userService.findById(id));
    }

    @Override
    public void save(final User user) {
        transactionTemplate.execute(connection -> {
            userService.save(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.execute(connection -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
