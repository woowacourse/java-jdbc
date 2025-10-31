package com.techcourse.service;

import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService{

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(long id) {
        return transactionTemplate.returnInTransaction(() ->
                userService.findById(id)
        );
    }

    @Override
    public User findByAccount(String account) {
        return transactionTemplate.returnInTransaction(() ->
                userService.findByAccount(account)
        );
    }

    @Override
    public void insert(User user) {
        transactionTemplate.doInTransaction(() ->
                userService.insert(user)
        );
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionTemplate.doInTransaction(() ->
                userService.changePassword(id, newPassword, createBy)
        );
    }
}
