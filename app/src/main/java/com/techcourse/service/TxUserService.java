package com.techcourse.service;

import com.interface21.transaction.support.BusinessService;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService extends BusinessService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
        super(DataSourceConfig.getInstance());
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.
    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        transaction(() -> {
            userService.changePassword(id, newPassword, createdBy);
            return null;
        });
    }
}
