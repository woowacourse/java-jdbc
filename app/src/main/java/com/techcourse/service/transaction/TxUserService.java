package com.techcourse.service.transaction;

import com.interface21.transaction.manager.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.service.UserService;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public User findByAccount(String account) {
        return userService.findByAccount(account);
    }

    @Override
    public void save(User user) {
        TransactionManager.runTransaction(() -> userService.save(user), DataSourceConfig.getInstance());
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        TransactionManager.runTransaction(() -> userService.changePassword(id, newPassword, createdBy), DataSourceConfig.getInstance());
    }
}
