package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.TransactionalExecutor;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionalExecutor transactionalExecutor;

    public TxUserService(UserService userService, TransactionalExecutor transactionalExecutor) {
        this.userService = userService;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionalExecutor.execute((con) ->
            userService.changePassword(id, newPassword, createBy)
        );
    }
}
