package com.techcourse.service;

import com.interface21.jdbc.core.Transaction;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        final var transaction = Transaction.init(dataSource);
        try {
            transaction.begin();
            userService.changePassword(id, newPassword, createdBy);
            transaction.commit();
        } catch (final Exception e) {
            transaction.rollback();
            throw e;
        }
    }
}
