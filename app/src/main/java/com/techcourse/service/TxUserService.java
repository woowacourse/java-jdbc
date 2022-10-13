package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

public class TxUserService implements UserService {

    private final TransactionTemplate transactionTemplate;
    private final UserService userService;

    public TxUserService(final PlatformTransactionManager transactionManager,
                         final UserService userService) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return transactionTemplate.doTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionTemplate.doTransaction(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.doTransaction(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
