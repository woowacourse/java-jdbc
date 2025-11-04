package com.techcourse.service;

import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionTemplate.execute(() -> userService.changePassword(id, newPassword, createdBy));
    }
}
