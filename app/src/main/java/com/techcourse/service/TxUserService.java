package com.techcourse.service;

import com.interface21.transaction.SimpleTransactionManager;
import com.interface21.transaction.TransactionException;
import com.interface21.transaction.TransactionManager;
import com.techcourse.domain.User;

import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService delegate;
    private final TransactionManager txManager;

    public TxUserService(final UserService delegate, final DataSource dataSource) {
        this.delegate = delegate;
        this.txManager = new SimpleTransactionManager(dataSource);
    }

    @Override
    public User findById(final Long id) {
        return delegate.findById(id);
    }

    @Override
    public User findByAccount(final String account) {
        return delegate.findByAccount(account);
    }

    @Override
    public void save(final User user) {
        delegate.save(user);
    }

    @Override
    public void changePassword(final Long id, final String newPassword, final String createdBy) {
        txManager.begin();
        try {
            delegate.changePassword(id, newPassword, createdBy);
            txManager.commit();
        } catch (final RuntimeException | Error e) {
            try {
                txManager.rollback();
            } catch (final TransactionException rollbackEx) {
                rollbackEx.addSuppressed(e);
                throw rollbackEx;
            }
            throw e;
        } finally {
            txManager.cleanup();
        }
    }
}
