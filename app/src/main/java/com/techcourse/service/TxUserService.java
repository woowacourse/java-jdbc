package com.techcourse.service;

import com.interface21.transaction.support.PlatformTransactionManager;
import com.techcourse.domain.User;
import java.sql.SQLException;

public class TxUserService implements  UserService {

    private final UserService userService;
    private final PlatformTransactionManager transactionManager;

    public TxUserService(final UserService userService, final PlatformTransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
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
    public void changePassword(long id, String newPassword, String createBy) {
        try {
            transactionManager.getTransaction();
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit();
        } catch (RuntimeException | SQLException exception) {
            transactionManager.rollback();
        }
    }
}
