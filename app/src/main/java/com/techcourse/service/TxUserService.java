package com.techcourse.service;

import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService; // 타겟 객체 (AppUserService)
    private final TransactionTemplate transactionTemplate;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        final DataSource dataSource = DataSourceConfig.getInstance();
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Override
    public User findById(final long id) {
        return (User) transactionTemplate.execute(() -> userService.findById(id));
    }

    @Override
    public void save(final User user) {
        transactionTemplate.execute(() -> {
            userService.save(user);
            return null;
        });
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionTemplate.execute(() -> {
            userService.changePassword(id, newPassword, createdBy);
            return null;
        });
    }
}
