package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionalExecutor transactionalExecutor;

    public TxUserService(final UserService userService, final TransactionalExecutor transactionalExecutor) {
        this.userService = userService;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.
    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionalExecutor.execute(
                () -> userService.changePassword(id, newPassword, createBy),
                DataSourceConfig.getInstance()
        );
    }
}
