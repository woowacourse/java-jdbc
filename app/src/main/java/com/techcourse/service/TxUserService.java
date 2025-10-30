package com.techcourse.service;

import com.techcourse.domain.User;
import com.techcourse.support.transaction.TransactionInterceptor;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionInterceptor transactionInterceptor;

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.transactionInterceptor = new TransactionInterceptor(dataSource);
    }

    @Override
    public User findById(long id) {
        return transactionInterceptor.applyTransaction(() -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        transactionInterceptor.applyTransaction(() -> {
            userService.save(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionInterceptor.applyTransaction(() -> {
            userService.changePassword(id, newPassword, createdBy);
            return null;
        });
    }
}
