package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.transaction.TransactionContext;

public class TxUserService implements UserService {

    private final UserService implementedService;
    private final TransactionContext txContext;

    public TxUserService(final TransactionContext txContext, final UserService implementedService) {
        this.implementedService = implementedService;
        this.txContext = txContext;
    }

    @Override
    public User findById(final long id) {
        return txContext.runFunction(() -> implementedService.findById(id));
    }

    @Override
    public void insert(final User user) {
        txContext.runConsumer(() -> implementedService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        txContext.runConsumer(() ->implementedService.changePassword(id, newPassword, createBy));
    }
}
