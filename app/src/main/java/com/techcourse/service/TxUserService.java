package com.techcourse.service;

import com.interface21.transaction.TransactionTemplate;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final TransactionTemplate transactionTemplate;
    private final UserService userService;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Override
    public User findById(final long id) {
        return transactionTemplate.execute(() -> userService.findById(id));
    }

    @Override
    public void save(final User user) {
        transactionTemplate.execute(() -> {
            userService.save(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.execute(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
