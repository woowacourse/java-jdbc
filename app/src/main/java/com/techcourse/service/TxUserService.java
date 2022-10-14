package com.techcourse.service;

import com.techcourse.domain.User;
import java.util.Optional;
import nextstep.transaction.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

public class TxUserService implements UserService {

    private final TransactionTemplate transactionTemplate;
    private final AppUserService appUserService;

    public TxUserService(final PlatformTransactionManager transactionManager, final AppUserService appUserService) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.appUserService = appUserService;
    }

    @Override
    public User findById(final long id) {
        return transactionTemplate.executeTransaction(() -> appUserService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionTemplate.executeTransaction(() -> {
            appUserService.insert(user);
            return Optional.empty();
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.executeTransaction(() -> {
            appUserService.changePassword(id, newPassword, createBy);
            return Optional.empty();
        });
    }
}
