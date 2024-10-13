package com.techcourse.service;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Inject;
import com.interface21.jdbc.TransactionManager;
import com.techcourse.UserService;
import com.techcourse.domain.User;

@Component
public class TxUserService implements UserService {

    @Inject
    private UserService userService;

    @Inject
    private TransactionManager transactionManager;

    private TxUserService() {}

    public TxUserService(UserService userService, TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public User findByAccount(String account) {
        return userService.findByAccount(account);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.execute(() -> userService.changePassword(id, newPassword, createBy));
    }
}
