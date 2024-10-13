package com.techcourse.service;

import com.interface21.jdbc.datasource.ConnectionExecutor;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        ConnectionExecutor.executeTransactional(dataSource,
                () -> userService.changePassword(id, newPassword, createBy));
    }

    @Override
    public void insert(User user) {
        ConnectionExecutor.execute(dataSource, () -> userService.insert(user));
    }

    @Override
    public User findById(long id) {
        return ConnectionExecutor.supply(dataSource, () -> userService.findById(id));
    }
}
