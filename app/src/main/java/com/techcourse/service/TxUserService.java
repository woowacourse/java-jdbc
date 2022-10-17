package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final AppUserService appUserService;

    public TxUserService(final PlatformTransactionManager transactionManager,
                         final AppUserService appUserService) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    @Override
    public User findById(final long id) {
        return appUserService.findById(id);
    }

    @Override
    public void insert(User user) {
        execute(() -> appUserService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        execute(() -> appUserService.changePassword(
                id,
                newPassword,
                createBy
        ));
    }

    private void execute(final Runnable runnable) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());

        try {
            runnable.run();
            transactionManager.commit(transactionStatus);
        } catch (final RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        } catch (final Exception e) {
            transactionManager.commit(transactionStatus);
        }
    }
}
