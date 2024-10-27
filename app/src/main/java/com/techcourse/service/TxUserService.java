package com.techcourse.service;

import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import javax.sql.DataSource;

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
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        TransactionTemplate.executeTransaction(dataSource,
                () -> userService.changePassword(id, newPassword, createdBy)
        );
    }
}
