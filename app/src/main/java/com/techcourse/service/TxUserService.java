package com.techcourse.service;

import com.interface21.transaction.TransactionTemplate;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService target;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(
            final UserService target,
            final DataSource dataSource
    ) {
        this.target = target;
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Override
    public User findById(final long id) {
        return target.findById(id);
    }

    @Override
    public void insert(final User user) {
        target.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.execute(() -> target.changePassword(id, newPassword, createBy));
    }
}
