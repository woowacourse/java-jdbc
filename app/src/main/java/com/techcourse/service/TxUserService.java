package com.techcourse.service;

import com.interface21.template.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.service.model.UserService;

import java.util.Optional;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
    }

    @Override
    public User findById(long id) {
        return transactionTemplate.doTransaction(
                () -> userService.findById(id)
        );
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return transactionTemplate.doTransaction(
                () -> userService.findByAccount(account)
        );
    }

    @Override
    public void save(User user) {
        transactionTemplate.doTransaction(
                () -> userService.save(user)
        );
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        transactionTemplate.doTransaction(
                () -> userService.changePassword(id, newPassword, createdBy)
        );
    }
}
