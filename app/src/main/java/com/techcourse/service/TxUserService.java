package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager txManager;
    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.txManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var tx = txManager.getTransaction(new DefaultTransactionDefinition());

        try {
            userService.changePassword(id, newPassword, createBy);
            txManager.commit(tx);
        } catch (DataAccessException e) {
            txManager.rollback(tx);
            throw new DataAccessException(e);
        }
    }
}
