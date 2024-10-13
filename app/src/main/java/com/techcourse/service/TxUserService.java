package com.techcourse.service;


import com.interface21.jdbc.core.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        DataSource dataSource = DataSourceConfig.getInstance();
        return TransactionManager.doInTransaction(dataSource, () -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        DataSource dataSource = DataSourceConfig.getInstance();
        TransactionManager.doInTransaction(dataSource, () -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        TransactionManager.doInTransaction(dataSource, () -> {
            userService.changePassword(id, newPassword, createBy);
        });
    }
}
