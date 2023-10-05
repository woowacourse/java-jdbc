package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        final DataSource dataSource = DataSourceConfig.getInstance();
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Override
    public User findById(final long id) {
        return null;
    }

    @Override
    public void insert(final User user) {
        transactionTemplate.executeWithTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.executeWithTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }
}
