package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.DataSourceTransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSourceTransactionManager transactionManager;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transactionManager.bindConnection();
        try {
            userService.changePassword(id, newPassword, createdBy);
            transactionManager.commit();
        } catch (final Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        }
    }
}
