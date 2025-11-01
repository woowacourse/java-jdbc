package com.techcourse.service;

import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(final UserService userService, final TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public User getById(final long id) {
        // 트랜잭션이 필요 없으므로, 그대로 위임
        return userService.getById(id);
    }

    @Override
    public void save(final User user) {
        transactionTemplate.execute(() -> {
            userService.save(user);
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionTemplate.execute(() -> {
            userService.changePassword(id, newPassword, createdBy);
        });
    }
}
