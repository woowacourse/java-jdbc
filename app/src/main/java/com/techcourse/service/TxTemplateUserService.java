package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.TxTemplate;

public class TxTemplateUserService implements UserService {

    private final UserService delegate;
    private final TxTemplate txTemplate;

    public TxTemplateUserService(UserService delegate, TxTemplate txTemplate) {
        this.delegate = delegate;
        this.txTemplate = txTemplate;
    }

    public User findById(final long id) {
        return delegate.findById(id);
    }

    public void insert(final User user) {
        delegate.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        txTemplate.execute(() -> {
            delegate.changePassword(id, newPassword, createBy);
        });
    }
}
