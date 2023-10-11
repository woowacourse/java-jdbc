package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionTemplate;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return TransactionTemplate.query(() -> userService.findById(id), DataSourceConfig.getInstance());
    }

    @Override
    public void insert(User user) {
        TransactionTemplate.execute(() -> userService.insert(user), DataSourceConfig.getInstance());
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        TransactionTemplate.execute(
                () -> userService.changePassword(id, newPassword, createBy), DataSourceConfig.getInstance()
        );
    }
}
