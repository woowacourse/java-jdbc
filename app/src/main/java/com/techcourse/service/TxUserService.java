package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionManger;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManger transactionManger;

    public TxUserService(AppUserService appUserService) {
        this.userService = appUserService;
        this.transactionManger = new TransactionManger(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(long id) {
        return transactionManger.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionManger.execute(() -> {
                    userService.insert(user);
                    return null;
                }
        );
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManger.execute(() -> {
                    userService.changePassword(id, newPassword, createBy);
                    return null;
                }
        );
    }
}
