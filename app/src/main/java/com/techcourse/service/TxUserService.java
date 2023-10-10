package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.transaction.TxExecutor;

import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(
            final DataSource dataSource,
            final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return (User) TxExecutor.execute(dataSource, () -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        TxExecutor.executeWithoutReturnValue(dataSource, () -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TxExecutor.executeWithoutReturnValue(dataSource, () -> userService.changePassword(id, newPassword, createBy));
    }
}
