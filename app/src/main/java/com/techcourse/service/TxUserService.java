package com.techcourse.service;

import com.interface21.transaction.TransactionManager;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService target;
    private final TransactionManager tx;

    public TxUserService(
            final UserService target,
            final DataSource dataSource
    ) {
        this.target = target;
        this.tx = new TransactionManager(dataSource);
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
        tx.begin();
        try {
            target.changePassword(id, newPassword, createBy);
            tx.commit();
        } catch (Exception e) {
            System.out.println("롤백");
            tx.rollback();
            throw e;
        }
    }
}
