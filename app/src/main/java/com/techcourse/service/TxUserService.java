package com.techcourse.service;

import javax.sql.DataSource;
import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final TransactionTemplate txTemplate;
    private final UserService userService;

    public TxUserService(DataSource dataSource, UserService userService) {
        this.txTemplate = new TransactionTemplate(dataSource);
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        txTemplate.executeWithoutResult(() -> userService.changePassword(id, newPassword, createdBy));
    }
}
